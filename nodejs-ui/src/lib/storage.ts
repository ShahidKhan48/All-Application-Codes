// Utility functions for localStorage get and set

import { fetchUserRolesAndPrivilegesAsync } from '@/configs/requests/login-service';

export function set<T>(key: string, value: T): void {
  try {
    localStorage.setItem(key, JSON.stringify(value));
  } catch (error) {
    // Optionally handle quota exceeded or other errors
    console.error('Error setting localStorage:', error);
  }
}

export function get<T>(key: string): T | null {
  try {
    const item = localStorage.getItem(key);
    if (item === null || item === 'undefined') return null;
    return JSON.parse(item) as T;
  } catch (error) {
    // Optionally handle JSON parse errors
    console.error('Error getting localStorage:', error);
    return null;
  }
}

// Types for clarity (customize as needed)
export interface UserIds {
  realmIdentifier: string;
  userId: string;
  token?: string;
  systemUserId?: string;
}

export interface UserRealmInfo {
  realmId: string;
  userId: string;
  [key: string]: unknown;
}

export interface AuthData {
  token: string;
  expiresAt?: number;
  [key: string]: unknown;
}

export interface Privileges {
  privileges: string[];
  [key: string]: unknown;
}

export function getUserIds(): UserIds | null {
  return get<UserIds>('user-ids');
}

export function getUserRealmInfo(): UserRealmInfo | null {
  return get<UserRealmInfo>('user-realm-info');
}

export function getAuthData(): AuthData | null {
  return get<AuthData>('authData');
}

export function getPrivileges(): string[] | [] {
  return get<string[]>('allowed-privileges');
}

export async function fetchAndStorePrivileges(): Promise<string[]> {
  try {
    const userIds = getUserIds();
    const userRealmInfo = getUserRealmInfo();

    if (!userIds || !userRealmInfo) {
      console.warn(
        'fetchAndStorePrivileges: User IDs or realm info not found, cannot fetch privileges'
      );
      return [];
    }

    // Import the API function dynamically to avoid circular dependencies

    const response = await fetchUserRolesAndPrivilegesAsync({
      input: {
        userId: userIds.userId,
        realmId: userRealmInfo.realmId,
      },
    });

    // Check if we have roles in the response - the correct structure is data.response.roles
    if (response?.data?.response?.roles) {
      const roles = response.data.response.roles;

      const privileges: string[] = roles.flatMap((role: any) => {
        if (
          role &&
          typeof role === 'object' &&
          'privileges' in role &&
          Array.isArray(role.privileges)
        ) {
          return role.privileges
            .map((p: any) => {
              if (
                p &&
                typeof p === 'object' &&
                'name' in p &&
                typeof p.name === 'string'
              ) {
                return p.name;
              }
              return undefined;
            })
            .filter((name): name is string => typeof name === 'string');
        }
        return [];
      });

      // Store the fetched privileges
      localStorage.setItem('allowed-privileges', JSON.stringify(privileges));
      return privileges;
    }

    // If no roles in response.data.response.roles, check if roles are in the root response
    if (response?.roles) {
      const roles = response.roles;

      const privileges: string[] = roles.flatMap((role: any) => {
        if (
          role &&
          typeof role === 'object' &&
          'privileges' in role &&
          Array.isArray(role.privileges)
        ) {
          return role.privileges
            .map((p: any) => {
              if (
                p &&
                typeof p === 'object' &&
                'name' in p &&
                typeof p.name === 'string'
              ) {
                return p.name;
              }
              return undefined;
            })
            .filter((name): name is string => typeof name === 'string');
        }
        return [];
      });

      // Store the fetched privileges
      localStorage.setItem('allowed-privileges', JSON.stringify(privileges));
      return privileges;
    }

    // If still no roles, check if privileges are directly in the response
    if (response?.privileges) {
      const privileges = response.privileges;

      const privilegeNames: string[] = privileges
        .map((p: any) => {
          if (
            p &&
            typeof p === 'object' &&
            'name' in p &&
            typeof p.name === 'string'
          ) {
            return p.name;
          }
          return undefined;
        })
        .filter((name): name is string => typeof name === 'string');

      // Store the fetched privileges
      localStorage.setItem(
        'allowed-privileges',
        JSON.stringify(privilegeNames)
      );
      return privilegeNames;
    }

    // As a fallback, try to extract privileges from the user-realm-info that's already in localStorage
    // since we can see it contains roles with privileges
    const authData = localStorage.getItem('authData');
    if (authData) {
      try {
        const parsedAuthData = JSON.parse(authData);
        if (parsedAuthData?.roles) {
          const privileges: string[] = parsedAuthData.roles.flatMap(
            (role: any) => {
              if (
                role &&
                typeof role === 'object' &&
                'privileges' in role &&
                Array.isArray(role.privileges)
              ) {
                return role.privileges
                  .map((p: any) => {
                    if (
                      p &&
                      typeof p === 'object' &&
                      'name' in p &&
                      typeof p.name === 'string'
                    ) {
                      return p.name;
                    }
                    return undefined;
                  })
                  .filter((name): name is string => typeof name === 'string');
              }
              return [];
            }
          );

          // Store the extracted privileges
          localStorage.setItem(
            'allowed-privileges',
            JSON.stringify(privileges)
          );
          return privileges;
        }
      } catch (error) {
        console.error(
          'fetchAndStorePrivileges: Error parsing authData:',
          error
        );
      }
    }

    return [];
  } catch (error) {
    console.error('fetchAndStorePrivileges: Error fetching privileges:', error);
    return [];
  }
}
