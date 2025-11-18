import { MapPinIcon, MapPinnedIcon } from "lucide-react";
import { MouseEventHandler } from "react";

function MapButton({ lat, lng }) {
  const openMap = (e: React.MouseEvent<SVGSVGElement, MouseEvent>) => {
    e.stopPropagation();
    const url = `https://www.google.com/maps?q=${lat},${lng}`;
    window.open(url, "_blank");
  };

  return <MapPinnedIcon size={24} onClick={openMap} />;
}
export default MapButton;
