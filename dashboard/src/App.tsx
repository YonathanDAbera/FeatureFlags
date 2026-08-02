import { useCallback, useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import './App.css'
import { flagApi, type AuditEvent, type Environment, type Evaluation, type FeatureFlag } from './api'
import { keycloak, rolesFromToken, startSession } from './auth'

const environments: Environment[] = ['development', 'staging', 'production']

const navItems = [
  ['⌘', 'Workspace', 'workspace'],
  ['⚑', 'Flag registry', 'registry'],
  ['◉', 'Evaluator', 'evaluator'],
]

function App() {
  const [sessionState, setSessionState] = useState<'loading' | 'ready' | 'error'>('loading')
  const [signedIn, setSignedIn] = useState(false)
  const [environment, setEnvironment] = useState<Environment>('development')
  const [flags, setFlags] = useState<FeatureFlag[]>([])
  const [selectedKey, setSelectedKey] = useState('')
  const [auditEvents, setAuditEvents] = useState<AuditEvent[]>([])
  const [evaluation, setEvaluation] = useState<Evaluation | null>(null)
  const [userId, setUserId] = useState('yonathan')
  const [rolloutDraft, setRolloutDraft] = useState(0)
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState('')
  const [showCreate, setShowCreate] = useState(false)
  const [newFlagKey, setNewFlagKey] = useState('')
  const [newFlagRollout, setNewFlagRollout] = useState(0)
  const [filterQuery, setFilterQuery] = useState('')
  const [apiStatus, setApiStatus] = useState<'connected' | 'issue'>('connected')

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

  useEffect(() => { void loadFlags() }, [loadFlags])
  useEffect(() => { void loadAudit() }, [loadAudit])
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
    <div className="app-shell">
      <div className="app-shell" inert={showCreate ? true : undefined} aria-hidden={showCreate || undefined}>
      <aside className="sidebar">
        <div className="brand"><span>FF</span><strong>FeatureFlag</strong></div>
        <nav>
          {navItems.map(([icon, label, target]) => (
            <button key={target} className={target === 'workspace' ? 'nav-item nav-item--active' : 'nav-item'} onClick={() => document.getElementById(target)?.scrollIntoView({ behavior: 'smooth' })}>
              <span>{icon}</span>{label}
            </button>
          ))}
        </nav>
        <div className="sidebar__footer">
          <span className={apiStatus === 'connected' ? 'live-dot' : 'live-dot live-dot--error'} /> {apiStatus === 'connected' ? 'API connected' : 'API issue'}
          <p>{apiStatus === 'connected' ? 'JWT session active' : 'Retrying on next request'}</p>
        </div>
      </aside>

      <main className="workspace" id="workspace">
        <header className="topbar">
          <div>
            <p className="eyebrow">Operator workspace</p>
            <h1>Ship with a clear signal.</h1>
          </div>
          <div className="topbar__actions">
            <label className="environment-picker">Environment
              <select value={environment} onChange={(event) => { setEnvironment(event.target.value as Environment); setEvaluation(null) }}>
                {environments.map((item) => <option key={item} value={item}>{item}</option>)}
              </select>
            </label>
            <button className="avatar" title="Sign out" onClick={() => keycloak.logout({ redirectUri: window.location.origin })}>{actor.slice(0, 1).toUpperCase()}</button>
          </div>
        </header>

        <div className="status-line" aria-live="polite">
          <span className="role-badge">{canManage ? 'ADMIN' : 'EVALUATOR'}</span>
          <span>{message || `${flags.length} flags in ${environment}`}</span>
          {busy && <span className="saving">Working…</span>}
        </div>

        <section className="operator-grid">
          <section className="panel registry" id="registry">
            <div className="panel__header">
              <div><p className="eyebrow">Registry</p><h2>Feature flags</h2></div>
              {canManage && <button className="button button--quiet" onClick={() => setShowCreate(true)}>+ New flag</button>}
            </div>
            <div className="search-line"><span>⌕</span><input aria-label="Search flags" placeholder="Filter flags" value={filterQuery} onChange={(event) => setFilterQuery(event.target.value)} /></div>
            <div className="flag-list">
              {filteredFlags.map((flag) => (
                <button key={flag.key} className={flag.key === selectedKey ? 'flag-row flag-row--selected' : 'flag-row'} onClick={() => { setSelectedKey(flag.key); setEvaluation(null) }}>
                  <span className={flag.enabled ? 'flag-state flag-state--on' : 'flag-state'} />
                  <span className="flag-row__name">{flag.key}</span>
                  <span className="flag-row__rollout">{flag.rolloutPercentage}%</span>
                </button>
              ))}
              {!flags.length && !busy && <div className="empty">No flags in this environment yet.</div>}
              {!!flags.length && !filteredFlags.length && <div className="empty">No flags match that filter.</div>}
            </div>
          </section>

          <section className="panel detail">
            {selectedFlag ? <>
              <div className="panel__header">
                <div><p className="eyebrow">Selected flag</p><h2>{selectedFlag.key}</h2></div>
                <label className={selectedFlag.enabled ? 'toggle toggle--on' : 'toggle'}>
                  <input type="checkbox" checked={selectedFlag.enabled} disabled={!canManage || busy} onChange={() => void updateFlag({ enabled: !selectedFlag.enabled, rolloutPercentage: selectedFlag.rolloutPercentage })} />
                  <span /><b>{selectedFlag.enabled ? 'Enabled' : 'Disabled'}</b>
                </label>
              </div>

              <div className="rollout-block">
                <div className="section-label"><span>Rollout percentage</span><strong>{rolloutDraft}%</strong></div>
                <input className="range" type="range" min="0" max="100" value={rolloutDraft} disabled={!canManage || busy} onChange={(event) => setRolloutDraft(Number(event.target.value))} />
                <div className="range-scale"><span>0%</span><span>50%</span><span>100%</span></div>
                {canManage && rolloutDraft !== selectedFlag.rolloutPercentage && <button className="button button--primary save-rollout" disabled={busy} onClick={() => void updateFlag({ enabled: selectedFlag.enabled, rolloutPercentage: rolloutDraft })}>Save rollout</button>}
              </div>

              <div className="details-divider" />
              <div className="targeting"><p className="eyebrow">Targeting</p><div><span className="rule-icon">↗</span><p><strong>Deterministic rollout</strong><br />Users are consistently bucketed by flag key and user ID.</p></div></div>
              <div className="audit"><div className="section-label"><span>Recent activity</span><button className="text-button" onClick={() => void loadAudit()}>Refresh</button></div>
                {auditEvents.slice(0, 4).map((event) => <AuditRow event={event} key={event.id} />)}
                {!auditEvents.length && <p className="muted">No activity recorded for this flag yet.</p>}
              </div>
            </> : <div className="empty detail-empty">Select a flag to inspect its rollout.</div>}
          </section>

          <section className="panel evaluator" id="evaluator">
            <div className="panel__header"><div><p className="eyebrow">Decision lab</p><h2>Evaluator</h2></div><span className="lab-dot" /></div>
            <p className="evaluator__copy">Confirm exactly how a specific user will experience the selected flag.</p>
            <form onSubmit={evaluateFlag}>
              <label>User identifier<input value={userId} onChange={(event) => setUserId(event.target.value)} placeholder="e.g. yonathan" /></label>
              <label>Environment<input value={environment} readOnly /></label>
              <button className="button button--primary evaluator__button" disabled={!selectedFlag || busy}>Evaluate user <span>→</span></button>
            </form>
            {evaluation ? <div className={evaluation.enabled ? 'decision decision--included' : 'decision'}>
              <div className="decision__symbol">{evaluation.enabled ? '✓' : '—'}</div>
              <p>{evaluation.enabled ? 'Included in rollout' : 'Excluded from rollout'}</p>
              <strong>{evaluation.enabled ? `${evaluation.rolloutPercentage}% rollout is active` : evaluation.reason.replaceAll('_', ' ').toLowerCase()}</strong>
              <span>Evaluated for {evaluation.userId}</span>
            </div> : <div className="decision decision--idle"><div className="decision__symbol">?</div><p>Awaiting evaluation</p><span>Choose a flag and test a user.</span></div>}
          </section>
        </section>
      </main>
      </div>

      {showCreate && <div className="modal-backdrop"><form className="modal" role="dialog" aria-modal="true" aria-labelledby="create-flag-title" onKeyDown={(event) => { if (event.key === 'Escape') setShowCreate(false) }} onSubmit={createFlag}>
        <button type="button" className="close" aria-label="Close" onClick={() => setShowCreate(false)}>×</button>
        <p className="eyebrow">New feature flag</p><h2 id="create-flag-title">Create a controlled rollout.</h2>
        <label>Flag key<input value={newFlagKey} onChange={(event) => setNewFlagKey(event.target.value)} placeholder="e.g. recommender-v2" autoFocus /></label>
        <label>Initial rollout <span>{newFlagRollout}%</span><input className="range" type="range" min="0" max="100" value={newFlagRollout} onChange={(event) => setNewFlagRollout(Number(event.target.value))} /></label>
        <button className="button button--primary" disabled={busy}>Create disabled flag</button>
      </form></div>}
    </div>
  )
}

function AuditRow({ event }: { event: AuditEvent }) {
  const state = event.newState.enabled ? 'enabled' : 'disabled'
  return <div className="audit-row"><span className={event.newState.enabled ? 'audit-mark audit-mark--on' : 'audit-mark'} />
    <p><strong>{event.action.replace('FLAG_', '').toLowerCase()}</strong> by {event.actor}<br /><span>{state}, {event.newState.rolloutPercentage}% rollout</span></p>
    <time>{new Intl.DateTimeFormat(undefined, { month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit' }).format(new Date(event.occurredAt))}</time>
  </div>
}

export default App
