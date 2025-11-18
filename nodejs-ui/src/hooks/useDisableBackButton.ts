import { useEffect } from "react";

const useDisableBackButton = () => {
  useEffect(() => {
    // Push a new state so that there's always a forward state
    window.history.pushState(null, "", window.location.href);

    const handlePopState = () => {
      // Prevent going back, push the same state again
      window.history.pushState(null, "", window.location.href);
    };

    window.addEventListener("popstate", handlePopState);

    return () => {
      window.removeEventListener("popstate", handlePopState);
    };
  }, []);
};

export default useDisableBackButton;
