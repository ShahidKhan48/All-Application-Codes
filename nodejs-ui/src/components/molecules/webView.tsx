import {
  getAuthData,
  getPrivileges,
  getUserIds,
  getUserRealmInfo,
} from "@/lib/storage";
import { Suspense, useEffect } from "react";
import Spinner from "../ui/spinner";

const WebView = ({
  src,
  id,
  onClose,
  additionalInfo,
}: {
  src: string;
  id?: string;
  onClose: (values: { close: boolean; success?: boolean }) => void;
  additionalInfo?: { agentDeliverOrderId: string };
}) => {
  const sessionData = {
    sourceApp: "agent",
    authData: getAuthData(),
    privileges: getPrivileges(),
    userIds: getUserIds(),
    userRealmInfo: getUserRealmInfo(),
    additionalInfo,
  };
  const elementId = id || "omniTradeUi";

  useEffect(() => {
    const closeHandler = (event) => {
      const { data } = event || {};
      const { close, success } = data || {};
      onClose({ close, success });
    };
    window.addEventListener("message", closeHandler);
    return () => {
      window.removeEventListener("message", closeHandler);
    };
  }, []);

  return (
    <div className="w-full h-full absolute top-0 right-0 left-0 bottom-0">
      <Suspense fallback={<Spinner label="Loading..." />}>
        <iframe
          id={elementId}
          src={src}
          className="w-full h-screen overflow-auto"
          onLoad={() => {
            const iframe = document.getElementById(
              elementId
            ) as HTMLIFrameElement;
            iframe.contentWindow.postMessage(sessionData, src);
          }}
        />
      </Suspense>
    </div>
  );
};
export default WebView;
