import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { fetchUserRolesAndPrivilegesAsync } from '@/configs/requests/login-service';

interface Realm {
  name: string;
  id: string;
}

interface AuthData {
  phoneNumber: string;
  realms: Realm[];
}

export default function SelectAccount() {
  const navigate = useNavigate();
  const [authData, setAuthData] = useState<AuthData | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const data = localStorage.getItem('authData');
    if (data) {
      try {
        const parsed = JSON.parse(data);
        setAuthData(parsed.data ? parsed.data : parsed);
      } catch {
        setAuthData(null);
      }
    }
  }, []);

  const handleSelect = async (realm: Realm) => {
    setLoading(true);
    setError(null);
    try {
      const data = localStorage.getItem('authData');
      if (!data) throw new Error('No auth data found');
      const parsed = JSON.parse(data);
      const auth = parsed.data ? parsed.data : parsed;

      const realmRelation: Record<string, unknown> = (
        auth.realmRelations as Record<string, unknown>[]
      ).find(
        (r) => (r as Record<string, unknown>).realmIdentifier === realm.id
      ) as Record<string, unknown>;
      if (!realmRelation) throw new Error('No realm relation found');

      const userId = String(realmRelation.userId);
      const realmId = String(realmRelation.realmIdentifier);

      const apiData: Record<string, unknown> =
        await fetchUserRolesAndPrivilegesAsync({ input: { userId, realmId } });
      if (!apiData.success)
        throw new Error(
          (apiData.errorMessage as string) || 'Failed to fetch roles/privileges'
        );

      const roles = Array.isArray(
        (
          (apiData.data as Record<string, unknown>)?.response as Record<
            string,
            unknown
          >
        )?.roles
      )
        ? ((
            (apiData.data as Record<string, unknown>)?.response as Record<
              string,
              unknown
            >
          )?.roles as any[])
        : [];

      const privileges: string[] = roles.flatMap((role) => {
        if (
          role &&
          typeof role === 'object' &&
          'privileges' in role &&
          Array.isArray((role as { privileges: unknown }).privileges)
        ) {
          return (role as { privileges: unknown[] }).privileges
            .map((p) => {
              if (
                p &&
                typeof p === 'object' &&
                'name' in p &&
                typeof (p as { name: unknown }).name === 'string'
              ) {
                return (p as { name: string }).name;
              }
              return undefined;
            })
            .filter((name): name is string => typeof name === 'string');
        }
        return [];
      });

      localStorage.setItem('allowed-privileges', JSON.stringify(privileges));
      localStorage.setItem(
        'user-ids',
        JSON.stringify({
          systemUserId: String(userId),
          userId: String(userId),
          realmIdentifier: realmId,
        })
      );

      const selectedAccount = { ...realmRelation, roles, realmId };
      const realmInfoToStore = {
        ...selectedAccount,
        primaryPhoneNumber: auth.phoneNumber,
      };
      localStorage.setItem('user-realm-info', JSON.stringify(realmInfoToStore));

      const authDataWithRoles = { ...auth, roles, realmId };
      localStorage.setItem('authData', JSON.stringify(authDataWithRoles));

      navigate('/dashboard');
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Something went wrong');
    } finally {
      setLoading(false);
    }
  };

  if (!authData) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-blue-600 text-white">
        <Card className="w-full max-w-sm bg-white shadow-lg rounded-2xl p-4">
          <CardHeader>
            <CardTitle className="text-center text-blue-600 text-lg font-bold">
              Account Selection
            </CardTitle>
          </CardHeader>
          <CardContent className="text-center text-gray-600">
            <p>No account data found. Please login again.</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-blue-600 p-4">
      <Card className="w-full max-w-sm rounded-2xl shadow-xl">
        <CardHeader className="text-center">
          <CardTitle className="text-xl font-bold text-blue-600">
            Select an Account
          </CardTitle>
          <div className="mt-1 text-gray-500 text-sm">
            Welcome, {authData.phoneNumber}
          </div>
        </CardHeader>
        <CardContent className="space-y-3">
          {error && <div className="text-red-500 text-center">{error}</div>}
          {authData.realms.map((realm) => (
            <Button
              key={realm.id}
              className="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg"
              onClick={() => handleSelect(realm)}
              disabled={loading}
            >
              {loading ? 'Loading...' : realm.name}
            </Button>
          ))}
        </CardContent>
      </Card>
    </div>
  );
}
