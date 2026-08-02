---
name: FeatureFlagTrials
description: A calm, precise operator workspace for trustworthy feature rollouts.
colors:
  graphite: "#1c2534"
  cool-canvas: "#f4f6f9"
  working-surface: "#ffffff"
  divider: "#e1e6ee"
  action-blue: "#3364e8"
  action-blue-hover: "#2958d8"
  status-green: "#51c991"
  muted-ink: "#738095"
typography:
  display:
    fontFamily: "DM Sans, ui-sans-serif, system-ui, sans-serif"
    fontSize: "clamp(25px, 3vw, 35px)"
    fontWeight: 600
    lineHeight: 1.05
    letterSpacing: "-0.045em"
  body:
    fontFamily: "DM Sans, ui-sans-serif, system-ui, sans-serif"
    fontSize: "12px"
    fontWeight: 400
    lineHeight: 1.55
  label:
    fontFamily: "DM Mono, ui-monospace, monospace"
    fontSize: "10px"
    fontWeight: 500
    letterSpacing: "0.095em"
rounded:
  control: "6px"
  surface: "11px"
spacing:
  compact: "8px"
  control: "10px"
  panel: "20px"
  workspace: "42px"
components:
  button-primary:
    backgroundColor: "{colors.action-blue}"
    textColor: "{colors.working-surface}"
    rounded: "{rounded.control}"
    padding: "10px 13px"
  button-primary-hover:
    backgroundColor: "{colors.action-blue-hover}"
  working-panel:
    backgroundColor: "{colors.working-surface}"
    rounded: "{rounded.surface}"
    padding: "{spacing.panel}"
---

# Design System: FeatureFlagTrials

## Overview

**Creative North Star: "The Quiet Control Room"**

FeatureFlagTrials favors informed action over decorative dashboard theater. It is a dense, ordered workspace: state is visible, a selected flag stays central, and the evaluator is always available to check the consequence of a change. The interface takes its quality bar from polished developer tools without borrowing any product's distinctive visual language.

**Key Characteristics:**

- Cool, low-noise operational surfaces with a single confident action color.
- Data-forward type hierarchy and compact, deliberate spacing.
- Role-aware controls that make unsafe actions unavailable rather than merely unexplained.

## Colors

The palette separates a quiet working field from decisive blue action and sparse green confirmation.

### Primary

- **Decision Blue:** Used for primary calls to action, selected registry state, and active range controls.

### Neutral

- **Graphite:** Used for headings and high-confidence information.
- **Cool Canvas:** Used behind the workbench to keep panels legible without adding visual drama.
- **Working Surface:** Used for all active operational panels.
- **Divider Mist:** Used for one-pixel separation between tasks and records.
- **Muted Ink:** Used for labels, explanatory copy, and secondary facts.

### Tertiary

- **Verification Green:** Used only for live, enabled, or included states.

**The One-Signal Rule.** Blue is reserved for an action, selection, or controlled value. Green is evidence of a positive system state, never a decorative accent.

## Typography

**Display Font:** DM Sans (with system sans-serif fallback)

**Label/Mono Font:** DM Mono (with system monospace fallback)

**Character:** The display face is compact and direct; the mono label face gives environment, role, and system metadata a precise technical register.

### Hierarchy

- **Display:** 600 weight with compressed tracking; reserved for the workspace promise and sign-in statement.
- **Headline:** 600 weight at 18px; used for panel titles and selected flag names.
- **Body:** 400 weight at 12px; used for explanations and audit facts.
- **Label:** 500 weight at 10px, uppercase with tracking; used for context before content.

**The Evidence-First Rule.** Labels never compete with names, state, percentage, or the decision result they describe.

## Layout

Desktop uses a durable three-column workbench: a compact registry, a dominant selected-flag panel, and an evaluator panel. The workspace has a fixed dark navigation rail and a centered, responsive content frame. At medium width, evaluator moves beneath the registry and inspector; at mobile width, all panels stack and the rail contracts to its brand mark.

## Elevation & Depth

Panels are flat at rest, divided by quiet borders and a very soft ambient shadow. Overlays alone receive pronounced depth so creating a flag clearly becomes a temporary mode.

**The Flat-By-Default Rule.** A border conveys ordinary containment; a larger shadow indicates a layer that needs attention.

## Shapes

Controls use gently rounded 6px corners, while working panels use an 11px radius. Circular status dots and avatar marks are the only fully round recurring shapes. Borders remain thin and cool rather than heavy or high contrast.

## Components

### Buttons

- **Shape:** Compact and gently curved (6px radius).
- **Primary:** Decision Blue with white text; used for sign-in, saving rollout, evaluating a user, and creating a flag.
- **Hover / Focus:** A slight upward movement on hover; form fields use a blue outline and pale blue focus ring.
- **Secondary / Ghost:** Pale blue background with blue text for low-risk adjacent actions.

### Cards / Containers

- **Corner Style:** Working panels use the surface radius (11px).
- **Background:** White panels on a Cool Canvas background.
- **Shadow Strategy:** Soft ambient shadow at rest; stronger elevation only for the modal.
- **Border:** One-pixel Divider Mist border.
- **Internal Padding:** Panel rhythm begins at 20px.

### Inputs / Fields

- **Style:** White fill, thin cool-gray stroke, compact 6px corners.
- **Focus:** A blue border with a 3px pale-blue ring.
- **Disabled:** Lower opacity and unavailable pointer behavior; permissions are visible in the session badge.

### Navigation

The navigation rail uses small labels, symbolic icons, and a pale active surface. The active route is the only bright navigation item; mobile preserves brand context while removing the long list.

### Decision State

The evaluator result is a compact evidence block. Included treatment receives Verification Green; excluded and waiting states remain neutral until the API supplies a decision.

## Do's and Don'ts

### Do:

- **Do** preserve the three-part mental model: find a flag, change or inspect it, then evaluate a user.
- **Do** expose role and environment close to the workspace header.
- **Do** make the selected state clear with blue tint, not by adding another card or modal.

### Don't:

- **Don't** use blue or green as broad decorative fills.
- **Don't** turn operational facts into oversized marketing statistics.
- **Don't** bury evaluator feedback behind a separate route when a selected flag is already in context.
