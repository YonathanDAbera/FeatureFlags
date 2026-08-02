import Keycloak from 'keycloak-js'

export const keycloak = new Keycloak({
  url: 'http://localhost:8081',
  realm: 'featureflags',
  clientId: 'featureflags-dashboard',
})

export async function startSession() {
  return keycloak.init({
    onLoad: 'check-sso',
    pkceMethod: 'S256',
    checkLoginIframe: false,
  })
}

export function rolesFromToken() {
  const realmAccess = keycloak.tokenParsed?.realm_access as { roles?: string[] } | undefined
  return realmAccess?.roles ?? []
}
