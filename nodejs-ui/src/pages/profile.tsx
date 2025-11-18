import ScreenHeader from "@/components/molecules/screen-header";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { useToast } from "@/hooks/use-toast";
import { getAuthData, getUserRealmInfo } from "@/lib/storage";
import { LogOut } from "lucide-react";
import { useNavigate } from "react-router-dom";
import ninjaLogoPng from "../components/assets/ninja-logo.png";
import Layout from "@/components/molecules/layout";

const ProfileIndex = () => {
  const { toast } = useToast();
  const navigate = useNavigate();
  const userInfo: any = getUserRealmInfo();
  const authData: any = getAuthData();

  const profileData = {
    name: userInfo?.name,
    partOf: authData?.realms?.find((m) => m.id === userInfo?.realmId)?.name,
    agentId: userInfo?.userId,
    contactNumber: `+91-${userInfo?.primaryPhoneNumber}`,
  };

  const handleLogout = () => {
    localStorage.clear();
    sessionStorage.clear();
    navigate("/");
    toast({
      title: "Logged out successfully",
      description: "You have been logged out of your account.",
    });
  };

  return (
    <Layout
      headerContent={
        <ScreenHeader title="Profile" onBack={() => navigate("/dashboard")} />
      }
      footerContent={
        <>
          {/* Logout Button */}
          <Button
            variant="destructive"
            size="lg"
            className="w-full rounded-2xl h-14 text-base"
            onClick={handleLogout}
          >
            <LogOut className="mr-2" size={20} />
            Logout
          </Button>

          {/* Footer */}
          <div className="mt-6 text-center">
            <p className="text-muted-foreground text-sm">
              © 2025 Ninjacart. All rights reserved.
            </p>
          </div>
        </>
      }
    >
      <Card className="bg-card border-border shadow-[var(--card-shadow)] rounded-3xl p-8 mb-6">
        {/* Logo */}
        <div className="flex justify-center mb-8">
          <div className="w-20 h-20 rounded-full overflow-hidden">
            <img
              src={ninjaLogoPng}
              alt="Ninjacart Logo"
              className="w-full h-full object-cover"
              loading="lazy"
              decoding="async"
            />
          </div>
        </div>

        {/* Divider */}
        <div className="w-full h-px bg-border mb-8"></div>

        {/* Profile Information */}
        <div className="space-y-6">
          <div className="flex flex-col items-start justify-start">
            <p className="text-label text-sm font-medium mb-1">Name</p>
            <p className="text-value text-lg font-semibold">
              {profileData.name}
            </p>
          </div>

          <div className="flex flex-col items-start justify-start">
            <p className="text-label text-sm font-medium mb-1">Part Of</p>
            <p className="text-value text-lg font-semibold">
              {profileData.partOf}
            </p>
          </div>

          <div className="flex flex-col items-start justify-start">
            <p className="text-label text-sm font-medium mb-1">Agent ID</p>
            <p className="text-value text-lg font-semibold">
              {profileData.agentId}
            </p>
          </div>

          <div className="flex flex-col items-start justify-start">
            <p className="text-label text-sm font-medium mb-1">
              Contact Number
            </p>
            <p className="text-value text-lg font-semibold">
              {profileData.contactNumber}
            </p>
          </div>
        </div>
      </Card>
    </Layout>
  );
};

export default ProfileIndex;
