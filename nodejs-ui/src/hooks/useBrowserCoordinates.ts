import { useEffect, useState } from "react";

interface Coordinates {
  lat: number | null;
  lng: number | null;
  error?: string;
}

export function useBrowserCoordinates() {
  const [coords, setCoords] = useState<Coordinates>({ lat: null, lng: null });

  useEffect(() => {
    if (!navigator.geolocation) {
      setCoords({ lat: null, lng: null, error: "Geolocation not supported" });
      return;
    }

    const watcher = navigator.geolocation.getCurrentPosition(
      (position) => {
        setCoords({
          lat: position.coords.latitude,
          lng: position.coords.longitude,
        });
      },
      (err) => {
        setCoords({ lat: null, lng: null, error: err.message });
      }
    );

    return () => {
      if (watcher && typeof watcher === "number") {
        navigator.geolocation.clearWatch(watcher);
      }
    };
  }, []);

  return coords;
}
