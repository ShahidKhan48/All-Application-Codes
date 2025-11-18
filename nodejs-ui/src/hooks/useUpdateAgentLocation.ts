import { useEffect } from "react";
import { useBrowserCoordinates } from "./useBrowserCoordinates";
import { updateAgentLocationAsync } from "@/configs/requests/dashbord-service";
import { getUserIds } from "@/lib/storage";
import { getTodayDate } from "@/lib/utils";

const useUpdateAgentLocation = (timeoutInMinutes: number = 2) => {
  const { lat, lng } = useBrowserCoordinates();

  useEffect(() => {
    let id: NodeJS.Timeout;

    if (lat && lng) {
      id = setTimeout(() => {
        const payload = {
          input: {
            agentId: getUserIds()?.systemUserId,
            date: getTodayDate(),
            latitude: String(lat),
            longitude: String(lng),
            timestamp: Date.now(),
          },
        };
        updateAgentLocationAsync(payload);
      }, 1000 * 60 * timeoutInMinutes);
    }

    return () => clearTimeout(id);
  }, [lat, lng]);
};
export default useUpdateAgentLocation;
