import { useCallback, useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import './App.css'
import { flagApi, type AuditEvent, type Environment, type Evaluation, type FeatureFlag, type TargetingRule } from './api'
import { keycloak, rolesFromToken, startSession } from './auth'

const environments: Environment[] = ['development', 'staging', 'production']
type DashboardView = 'flags' | 'evaluate' | 'audit'

function viewFromHash(): DashboardView {
  const view = window.location.hash.replace('#/', '')
  return view === 'evaluate' || view === 'audit' ? view : 'flags'
}

function App() {
  const [sessionState, setSessionState] = useState<'loading' | 'ready' | 'error'>('loading')
  const [signedIn, setSignedIn] = useState(false)
  const [environment, setEnvironment] = useState<Environment>('development')
  const [flags, setFlags] = useState<FeatureFlag[]>([])
  const [selectedKey, setSelectedKey] = useState('')
  const [auditEvents, setAuditEvents] = useState<AuditEvent[]>([])
  const [targetingRules, setTargetingRules] = useState<TargetingRule[]>([])
  const [ruleUserId, setRuleUserId] = useState('')
  const [evaluation, setEvaluation] = useState<Evaluation | null>(null)
  const [evaluationAt, setEvaluationAt] = useState<Date | null>(null)
  const [showContext, setShowContext] = useState(false)
  const [userId, setUserId] = useState('yonathan')
  const [rolloutDraft, setRolloutDraft] = useState(0)
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState('')
  const [showCreate, setShowCreate] = useState(false)
  const [newFlagKey, setNewFlagKey] = useState('')
  const [newFlagRollout, setNewFlagRollout] = useState(0)
  const [filterQuery, setFilterQuery] = useState('')
  const [apiStatus, setApiStatus] = useState<'connected' | 'issue'>('connected')
  const [activeView, setActiveView] = useState<DashboardView>(viewFromHash)
  const [showAllAudit, setShowAllAudit] = useState(false)

  const roles = useMemo(() => rolesFromToken(), [signedIn])
  const canManage = roles.includes('ADMIN')
  const actor = (keycloak.tokenParsed?.preferred_username as string | undefined) ?? 'dashboard-user'
  const selectedFlag = flags.find((flag) => flag.key === selectedKey) ?? null
  const filteredFlags = flags.filter((flag) => flag.key.toLowerCase().includes(filterQuery.trim().toLowerCase()))

  useEffect(() => {
    startSession()
      .then((authenticated) => {
        setSignedIn(authenticated)
        setSessionState('ready')
      })
      .catch(() => setSessionState('error'))
  }, [])

  useEffect(() => {
    const syncRoute = () => setActiveView(viewFromHash())
    window.addEventListener('hashchange', syncRoute)
    if (!window.location.hash) window.location.hash = '/flags'
    return () => window.removeEventListener('hashchange', syncRoute)
  }, [])

  function navigate(view: DashboardView) {
    window.location.hash = `/${view}`
  }

  const token = useCallback(async () => {
    await keycloak.updateToken(30)
    if (!keycloak.token) throw new Error('Your session has expired. Please sign in again.')
    return keycloak.token
  }, [])

  const loadFlags = useCallback(async () => {
    if (!signedIn) return
    setBusy(true)
    try {
      const response = await flagApi.list(environment, await token())
      const sorted = [...response].sort((a, b) => a.key.localeCompare(b.key))
      setFlags(sorted)
      setSelectedKey((current) => sorted.some((flag) => flag.key === current) ? current : (sorted[0]?.key ?? ''))
      setMessage('')
      setApiStatus('connected')
    } catch (error) {
      setApiStatus('issue')
      setMessage(error instanceof Error ? error.message : 'Could not load flags.')
    } finally {
      setBusy(false)
    }
  }, [environment, signedIn, token])

  const loadAudit = useCallback(async () => {
    if (!signedIn || !selectedKey) return
    try {
      const response = await flagApi.audit(environment, selectedKey, await token())
      setAuditEvents(response)
    } catch (error) {
      setApiStatus('issue')
      setMessage(error instanceof Error ? error.message : 'Could not load activity.')
    }
  }, [environment, selectedKey, signedIn, token])

  const loadRules = useCallback(async () => {
    if (!signedIn || !selectedKey) return
    try {
      setTargetingRules(await flagApi.rules(environment, selectedKey, await token()))
    } catch (error) {
      setApiStatus('issue')
      setMessage(error instanceof Error ? error.message : 'Could not load targeting rules.')
    }
  }, [environment, selectedKey, signedIn, token])

  useEffect(() => { void loadFlags() }, [loadFlags])
  useEffect(() => { void loadAudit() }, [loadAudit])
  useEffect(() => { void loadRules() }, [loadRules])
  useEffect(() => { if (selectedFlag) setRolloutDraft(selectedFlag.rolloutPercentage) }, [selectedFlag])

  async function updateFlag(change: Pick<FeatureFlag, 'enabled' | 'rolloutPercentage'>) {
    if (!selectedFlag || !canManage) return
    setBusy(true)
    try {
      const updated = await flagApi.update(environment, selectedFlag.key, change, await token(), actor)
      setFlags((current) => current.map((flag) => flag.key === updated.key ? updated : flag))
      setRolloutDraft(updated.rolloutPercentage)
      setMessage(`Saved ${updated.key}.`)
      await loadAudit()
    } catch (error) {
      setApiStatus('issue')
      setMessage(error instanceof Error ? error.message : 'Could not save the flag.')
    } finally {
      setBusy(false)
    }
  }

  async function evaluateFlag(event?: FormEvent) {
    event?.preventDefault()
    if (!selectedFlag || !userId.trim()) return
    setBusy(true)
    try {
      const result = await flagApi.evaluate(environment, selectedFlag.key, userId.trim(), await token())
      setEvaluation(result)
      setEvaluationAt(new Date())
      setMessage('Evaluation complete.')
    } catch (error) {
      setApiStatus('issue')
      setMessage(error instanceof Error ? error.message : 'Could not evaluate this user.')
    } finally {
      setBusy(false)
    }
  }

  async function createFlag(event: FormEvent) {
    event.preventDefault()
    if (!newFlagKey.trim()) return
    setBusy(true)
    try {
      const created = await flagApi.create(environment, {
        key: newFlagKey.trim(), enabled: false, rolloutPercentage: newFlagRollout,
      }, await token(), actor)
      setFlags((current) => [...current, created].sort((a, b) => a.key.localeCompare(b.key)))
      setSelectedKey(created.key)
      setNewFlagKey('')
      setNewFlagRollout(0)
      setShowCreate(false)
      setMessage(`Created ${created.key}.`)
    } catch (error) {
      setApiStatus('issue')
      setMessage(error instanceof Error ? error.message : 'Could not create the flag.')
    } finally {
      setBusy(false)
    }
  }

  async function addTargetingRule(event: FormEvent) {
    event.preventDefault()
    if (!selectedFlag || !ruleUserId.trim() || !canManage) return
    setBusy(true)
    try {
      const rule = await flagApi.addRule(environment, selectedFlag.key, { userId: ruleUserId.trim(), priority: targetingRules.length }, await token(), actor)
      setTargetingRules((current) => [...current, rule])
      setRuleUserId('')
      setMessage(`Added an inclusion rule for ${rule.userId}.`)
    } catch (error) {
      setApiStatus('issue')
      setMessage(error instanceof Error ? error.message : 'Could not add the targeting rule.')
    } finally {
      setBusy(false)
    }
  }

  async function removeTargetingRule(rule: TargetingRule) {
    if (!selectedFlag || !canManage) return
    setBusy(true)
    try {
      await flagApi.removeRule(environment, selectedFlag.key, rule.id, await token(), actor)
      setTargetingRules((current) => current.filter((currentRule) => currentRule.id !== rule.id))
      setMessage(`Removed the rule for ${rule.userId}.`)
    } catch (error) {
      setApiStatus('issue')
      setMessage(error instanceof Error ? error.message : 'Could not remove the targeting rule.')
    } finally {
      setBusy(false)
    }
  }

  if (sessionState === 'loading') return <div className="loading-screen">Starting the operator workspace…</div>

  if (sessionState === 'error') {
    return <div className="loading-screen"><strong>Couldn’t reach Keycloak.</strong><span>Start the local services, then refresh this page.</span></div>
  }

  if (!signedIn) {
    return (
      <main className="sign-in">
        <div className="sign-in__mark">FF</div>
        <p className="eyebrow">FeatureFlagTrials</p>
        <h1>Make every rollout<br />an informed decision.</h1>
        <p>Manage flags, inspect audit history, and verify a user’s treatment from one local workspace.</p>
        <button className="button button--primary" onClick={() => keycloak.login()}>Sign in with Keycloak <span>→</span></button>
        <small>Local demo: <strong>flag-admin / admin</strong> or <strong>flag-evaluator / evaluator</strong></small>
      </main>
    )
  }

  return (
    <div className="product-shell">
      <div className="product-shell__content" inert={showCreate ? true : undefined} aria-hidden={showCreate || undefined}>
        <header className="global-nav">
          <a className="wordmark" href="#workspace"><span>FF</span>FeatureFlagTrials</a>
          <nav aria-label="Product sections">
            <button className={activeView === 'flags' ? 'global-nav__active' : ''} onClick={() => navigate('flags')}>Flags</button>
            <button className={activeView === 'evaluate' ? 'global-nav__active' : ''} onClick={() => navigate('evaluate')}>Evaluate</button>
            <button className={activeView === 'audit' ? 'global-nav__active' : ''} onClick={() => navigate('audit')}>Audit</button>
          </nav>
          <div className="global-nav__right">
            <span className={apiStatus === 'connected' ? 'connection connection--up' : 'connection'}>{apiStatus === 'connected' ? 'System ready' : 'API issue'}</span>
            <span className="global-icon" title="API documentation"><Icon name="book" /></span>
            <span className="global-icon global-icon--notice" title="No new notifications"><Icon name="bell" /></span>
            <span className="global-icon" title="FeatureFlagTrials help"><Icon name="help" /></span>
            <button className="avatar" title="Sign out" onClick={() => keycloak.logout({ redirectUri: window.location.origin })}>{actor.slice(0, 1).toUpperCase()}</button>
          </div>
        </header>

        <main className={`workbench workbench--${activeView}`} id="workspace">
          <section className="registry" id="registry">
            <div className="registry__header"><h1>Flag registry</h1>{canManage && <button className="button button--primary button--new" onClick={() => setShowCreate(true)}><Icon name="plus" />New flag</button>}</div>
            <div className="registry-tools"><div className="search-line"><Icon name="search" /><input aria-label="Search flags" placeholder="Search flags…" value={filterQuery} onChange={(event) => setFilterQuery(event.target.value)} /></div><button className={filterQuery ? 'filter-button filter-button--active' : 'filter-button'} type="button" aria-label="Clear flag filter" title="Clear flag filter" onClick={() => setFilterQuery('')}><Icon name="filter" /></button></div>
            <p className="registry__count">{flags.length} flags in {environment}</p>
            <div className="flag-list">
              {filteredFlags.map((flag) => (
                <button key={flag.key} className={flag.key === selectedKey ? 'flag-row flag-row--selected' : 'flag-row'} onClick={() => { setSelectedKey(flag.key); setEvaluation(null) }}>
                  <span className="flag-mark">{flag.key.slice(0, 1).toUpperCase()}</span>
                  <span className="flag-row__copy"><strong>{flag.key}</strong><small>{flag.enabled ? 'Enabled rollout' : 'Disabled rollout'}</small></span>
                  <span className={flag.enabled ? 'flag-state flag-state--on' : 'flag-state'} />
                  <span className="flag-row__rollout">{flag.rolloutPercentage}%</span>
                </button>
              ))}
              {!flags.length && !busy && <div className="empty">No flags in this environment yet.</div>}
              {!!flags.length && !filteredFlags.length && <div className="empty">No flags match that filter.</div>}
            </div>
            <footer className="registry-footer">Showing {filteredFlags.length} of {flags.length} flags</footer>
          </section>

          <section className="detail">
            {selectedFlag ? <>
              <div className="crumbs">Flags <span>/</span> {selectedFlag.key}</div>
              <header className="flag-hero">
                <div><h2>{selectedFlag.key}</h2><p>Controlled rollout in <strong>{environment}</strong></p></div>
                <label className={selectedFlag.enabled ? 'toggle toggle--on' : 'toggle'}>
                  <input type="checkbox" checked={selectedFlag.enabled} disabled={!canManage || busy} onChange={() => void updateFlag({ enabled: !selectedFlag.enabled, rolloutPercentage: selectedFlag.rolloutPercentage })} />
                  <span /><b>{selectedFlag.enabled ? 'Enabled' : 'Disabled'}</b>
                </label>
              </header>
              <div className="flag-meta"><span>Environment <strong>{environment}</strong></span><span>Stable user bucketing</span><span className="role-badge">{canManage ? 'ADMIN' : 'EVALUATOR'}</span></div>

              <div className="rollout-block">
                <div className="section-title"><div><h3>Rollout</h3><p>Percentage of consistently bucketed users included in this flag.</p></div><div className="rollout-number"><strong>{rolloutDraft}</strong><span>%</span></div></div>
                <div className="rollout-control"><button type="button" aria-label="Decrease rollout" disabled={!canManage || rolloutDraft === 0} onClick={() => setRolloutDraft((current) => Math.max(0, current - 1))}>−</button><div className="rollout-input"><input aria-label="Rollout percentage" type="number" min="0" max="100" value={rolloutDraft} disabled={!canManage || busy} onChange={(event) => setRolloutDraft(Math.max(0, Math.min(100, Number(event.target.value))))} /><span>%</span></div><button type="button" aria-label="Increase rollout" disabled={!canManage || rolloutDraft === 100} onClick={() => setRolloutDraft((current) => Math.min(100, current + 1))}>+</button></div>
                <div className="rollout-rail"><span style={{ width: `${rolloutDraft}%` }} /></div>
                <div className="rollout-footer"><span>{rolloutDraft}% of eligible users</span>{canManage && rolloutDraft !== selectedFlag.rolloutPercentage && <button className="text-button" disabled={busy} onClick={() => void updateFlag({ enabled: selectedFlag.enabled, rolloutPercentage: rolloutDraft })}>Save rollout</button>}</div>
              </div>

              <section className="targeting-rules"><div className="targeting-rules__header"><div><h3>Targeting rules</h3><p>Matching users are included before percentage rollout.</p></div><span>{targetingRules.length} active</span></div>
                {targetingRules.length ? <div className="rule-list">{targetingRules.map((rule, index) => <div className="rule-row" key={rule.id}><span className="rule-order">{index + 1}</span><span><strong>User ID is</strong><small>{rule.userId}</small></span><b>Include</b>{canManage && <button className="remove-rule" type="button" aria-label={`Remove rule for ${rule.userId}`} title="Remove targeted user" disabled={busy} onClick={() => void removeTargetingRule(rule)}><Icon name="trash" /></button>}</div>)}</div> : <p className="rules-empty">No user targeting rules. Evaluations use the rollout percentage.</p>}
                {canManage && <form className="add-rule" onSubmit={addTargetingRule}><input value={ruleUserId} onChange={(event) => setRuleUserId(event.target.value)} placeholder="User ID to include" aria-label="User ID to include" /><button className="text-button" disabled={busy || !ruleUserId.trim()}><Icon name="plus" />Add rule</button></form>}
              </section>
              <div className="audit" id="audit"><div className="audit__header"><div><h3>Audit timeline</h3><p>Configuration history for this flag.</p></div><button className="text-button" onClick={() => void loadAudit()}>Refresh</button></div>
                {(activeView === 'audit' || showAllAudit ? auditEvents : auditEvents.slice(0, 4)).map((event) => <AuditRow event={event} key={event.id} />)}
                {!auditEvents.length && <p className="muted">No activity recorded for this flag yet.</p>}
                {activeView !== 'audit' && auditEvents.length > 4 && <button className="show-audit" type="button" onClick={() => setShowAllAudit((showing) => !showing)}>{showAllAudit ? 'Show recent activity' : `Show all ${auditEvents.length} events`}</button>}
              </div>
            </> : <div className="empty detail-empty">Select a flag to inspect its rollout.</div>}
          </section>

          <section className="evaluator" id="evaluator">
            <header className="evaluator__header"><div><h2>Evaluate</h2><p>Test the selected flag for a specific user.</p></div><button className="clear-button" type="button" onClick={() => { setEvaluation(null); setEvaluationAt(null) }}>Clear</button></header>
            <form onSubmit={evaluateFlag}>
              <label>User identifier<div className="input-shell"><input value={userId} onChange={(event) => setUserId(event.target.value)} placeholder="e.g. yonathan" /><Icon name="copy" /></div></label>
              <label>Environment<select value={environment} onChange={(event) => { setEnvironment(event.target.value as Environment); setEvaluation(null) }}>{environments.map((item) => <option key={item} value={item}>{item}</option>)}</select></label>
              <button className="context-toggle" type="button" onClick={() => setShowContext((open) => !open)}><Icon name={showContext ? 'chevronUp' : 'chevronDown'} />Advanced context <span>{showContext ? 'Hide' : 'Show'}</span></button>
              {showContext && <div className="advanced-context"><span><b>Flag</b>{selectedFlag?.key ?? '—'}</span><span><b>Role</b>{canManage ? 'Administrator' : 'Evaluator'}</span><span><b>Algorithm</b>Stable bucket</span></div>}
              <button className="button button--primary evaluator__button" disabled={!selectedFlag || busy}>Evaluate user <Icon name="arrow" /></button>
            </form>
            {evaluation ? <div className={evaluation.enabled ? 'decision decision--included' : 'decision'}>
              <div className="decision__symbol"><Icon name={evaluation.enabled ? 'check' : 'minus'} /></div>
              <p>{evaluation.enabled ? 'Included in rollout' : 'Excluded from rollout'}</p>
              <strong>{evaluation.enabled ? 'This user will receive the feature.' : evaluation.reason.replaceAll('_', ' ').toLowerCase()}</strong>
              <dl><div><dt>{evaluation.matchedRuleId ? 'Matched rule' : 'Rollout'}</dt><dd>{evaluation.matchedRuleId ? `#${evaluation.matchedRuleId}` : `${evaluation.rolloutPercentage}%`}</dd></div><div><dt>{evaluation.bucket === null ? 'Decision' : 'Bucket'}</dt><dd>{evaluation.bucket === null ? 'Rule match' : `${evaluation.bucket} / 100`}</dd></div><div><dt>User</dt><dd>{evaluation.userId}</dd></div></dl>
            </div> : <div className="decision decision--idle"><div className="decision__symbol"><Icon name="spark" /></div><p>Ready to evaluate</p><span>Choose a flag and test a user.</span></div>}
            {evaluation && <section className="raw-result"><div><h3>Evaluation details</h3><Icon name="copy" /></div><pre>{JSON.stringify(evaluation, null, 2)}</pre></section>}
            <div className="evaluation-note">{evaluationAt ? `Evaluated ${evaluationAt.toLocaleTimeString([], { hour: 'numeric', minute: '2-digit', second: '2-digit' })}` : 'JWT session active'} <span>Role: <strong>{canManage ? 'Administrator' : 'Evaluator'}</strong></span>{message && <em>{message}</em>}</div>
          </section>
        </main>
      </div>

      {showCreate && <div className="modal-backdrop"><form className="modal" role="dialog" aria-modal="true" aria-labelledby="create-flag-title" onKeyDown={(event) => { if (event.key === 'Escape') setShowCreate(false) }} onSubmit={createFlag}>
        <button type="button" className="close" aria-label="Close" onClick={() => setShowCreate(false)}>×</button>
        <h2 id="create-flag-title">Create a controlled rollout.</h2>
        <label>Flag key<input value={newFlagKey} onChange={(event) => setNewFlagKey(event.target.value)} placeholder="e.g. recommender-v2" autoFocus /></label>
        <label>Initial rollout <span>{newFlagRollout}%</span><input className="range" type="range" min="0" max="100" value={newFlagRollout} onChange={(event) => setNewFlagRollout(Number(event.target.value))} /></label>
        <button className="button button--primary" disabled={busy}>Create disabled flag</button>
      </form></div>}
    </div>
  )
}

function AuditRow({ event }: { event: AuditEvent }) {
  const state = event.newState.enabled ? 'enabled' : 'disabled'
  const actionLabel = event.action === 'FLAG_CREATED' ? 'Created' : event.action === 'TARGETING_RULE_ADDED' ? 'Rule added' : event.action === 'TARGETING_RULE_REMOVED' ? 'Rule removed' : event.newState.enabled ? 'Rolled out' : 'Disabled'
  const detail = event.details?.replace('userId=', 'targeted user: ') ?? `${state}, ${event.newState.rolloutPercentage}% rollout`
  return <div className="audit-row"><span className={event.newState.enabled ? 'audit-mark audit-mark--on' : 'audit-mark'} />
    <p><strong>{actionLabel.toLowerCase()}</strong> by {event.actor}<br /><span>{detail}</span></p>
    <span className={event.newState.enabled ? 'audit-badge audit-badge--on' : 'audit-badge'}>{actionLabel}</span>
    <time>{new Intl.DateTimeFormat(undefined, { month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit' }).format(new Date(event.occurredAt))}</time>
  </div>
}

function Icon({ name }: { name: 'plus' | 'search' | 'copy' | 'arrow' | 'check' | 'minus' | 'spark' | 'book' | 'bell' | 'help' | 'filter' | 'chevronUp' | 'chevronDown' | 'trash' }) {
  const paths = {
    plus: <><path d="M12 5v14M5 12h14" /></>,
    search: <><circle cx="11" cy="11" r="6" /><path d="m16 16 4 4" /></>,
    copy: <><rect x="9" y="9" width="10" height="10" rx="2" /><path d="M15 9V7a2 2 0 0 0-2-2H7a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h2" /></>,
    arrow: <><path d="M5 12h14M13 6l6 6-6 6" /></>,
    check: <path d="m5 12 4.5 4.5L19 7" />,
    minus: <path d="M5 12h14" />,
    spark: <><path d="m12 3 1.7 5.3L19 10l-5.3 1.7L12 17l-1.7-5.3L5 10l5.3-1.7L12 3Z" /><path d="m19 16 .7 2.3L22 19l-2.3.7L19 22l-.7-2.3L16 19l2.3-.7L19 16Z" /></>,
    book: <><path d="M4 5.5A2.5 2.5 0 0 1 6.5 3H11v17H6.5A2.5 2.5 0 0 0 4 22V5.5Z" /><path d="M20 5.5A2.5 2.5 0 0 0 17.5 3H13v17h4.5A2.5 2.5 0 0 1 20 22V5.5Z" /></>,
    bell: <><path d="M18 9a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 22h4" /></>,
    help: <><circle cx="12" cy="12" r="9" /><path d="M9.5 9a2.6 2.6 0 1 1 4.6 1.7c-.9 1.1-2.1 1.5-2.1 3.3M12 17h.01" /></>,
    filter: <path d="M4 5h16l-6.2 7.1v5.2l-3.6 1.7v-6.9L4 5Z" />,
    chevronUp: <path d="m7 14 5-5 5 5" />,
    chevronDown: <path d="m7 10 5 5 5-5" />,
    trash: <><path d="M4 7h16M10 11v6M14 11v6M6 7l1 14h10l1-14M9 7V4h6v3" /></>,
  }
  return <svg className="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">{paths[name]}</svg>
}

export default App
