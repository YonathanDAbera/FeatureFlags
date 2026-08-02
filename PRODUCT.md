# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Stack

Spring Boot backend with a React, Vite, and TypeScript dashboard in `dashboard/`.

## Users

Administrators manage feature flags, environment-specific rollout settings, and audit history. Evaluators test a flag decision for a user without changing configuration.

## Product Purpose

FeatureFlagTrials is a feature-flag control plane that lets teams roll out changes safely and inspect the decision path behind each rollout.

## Positioning

The product combines deterministic user rollout evaluation with environment isolation, transactional audit history, PostgreSQL persistence, and role-based access in one locally runnable system.

## Operating Context

Administrators work across development, staging, and production environments. They create and update flags, review change history, and use a Keycloak-issued JWT. Evaluators run read-only user decision checks.

## Capabilities and Constraints

The backend exposes environment-scoped REST APIs, JWT role authorization, Redis-backed evaluation caching, and Prometheus-ready metrics. The dashboard must work with the existing backend contracts and distinguish admin actions from evaluator actions.

## Evidence on Hand

The repository contains a runnable Spring Boot API, Docker Compose services for PostgreSQL, Redis, and Keycloak, plus real seeded feature-flag data. No existing visual assets or design system are available.

## Product Principles

- Make rollout state obvious at a glance.
- Preserve safe boundaries between environments and roles.
- Explain decisions with concrete audit evidence.
- Keep operational workflows fast and low-risk.

