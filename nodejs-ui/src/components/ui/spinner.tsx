import { Loader2 } from "lucide-react";

const Spinner = ({
  label,
  className,
  size = "lg",
}: {
  label?: string;
  className?: string;
  size?: "sm" | "lg";
}) => {
  return (
    <div
      className={`flex flex-col h-full items-center justify-center ${
        className || ""
      }`}
    >
      <Loader2 className="h-10 w-10 animate-spin mr-2" size={size} />
      {label}
    </div>
  );
};

export default Spinner;
