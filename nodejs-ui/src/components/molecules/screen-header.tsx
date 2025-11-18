import { ArrowLeft } from "lucide-react";
import { Button } from "../ui/button";

const ScreenHeader = ({
  title,
  onBack,
}: {
  title: string;
  onBack: () => void;
}) => {
  return (
    <div className="flex items-center gap-3">
      <Button
        variant="ghost"
        size="icon"
        onClick={onBack}
        className="text-white hover:bg-white/20"
      >
        <ArrowLeft className="w-5 h-5" />
      </Button>
      <h1 className="text-xl font-semibold">{title}</h1>
    </div>
  );
};

export default ScreenHeader;
