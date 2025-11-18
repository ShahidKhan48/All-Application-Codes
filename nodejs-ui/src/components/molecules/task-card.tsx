import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Calendar,
  CheckCircle,
  Clock,
  Truck,
  Package,
  FileCheck,
  CrossIcon,
  HandCoins,
} from "lucide-react";
import { cancelAgentTaskAsync } from "@/configs/requests/tasks-services";
import { closeAgentTaskAsync } from "@/configs/requests/dashbord-service";
import { TaskTypeEnum } from "@/lib/dashborad-type";

export type TaskStatus = "COMPLETED" | "IN_PROGRESS" | "PENDING" | "CANCELLED";
export type TaskType =
  | "STORE_VISIT"
  | "STORE_AUDIT"
  | "DELIVERY"
  | "DELIVERY_VERIFICATION"
  | "DUE_COLLECTION";

interface TaskCardProps {
  id: string;
  orderId?: string;
  type: TaskType;
  status: TaskStatus;
  onAction?: () => void;
  onClose?: () => void;
  onCancelTask: (id) => void;
  isPending: boolean;
}

const taskTypeConfig = {
  STORE_VISIT: {
    label: "End of Visit",
    icon: Package,
  },
  STORE_AUDIT: {
    label: "Create Order",
    icon: Package,
  },
  DELIVERY: {
    label: "Delivery",
    icon: Truck,
  },
  DELIVERY_VERIFICATION: {
    label: "Delivery Verification",
    icon: FileCheck,
  },
  DUE_COLLECTION: {
    label: "Collection Due",
    icon: HandCoins,
  },
};

const statusConfig = {
  COMPLETED: {
    label: "Completed",
    variant: "success" as const,
    icon: CheckCircle,
  },
  IN_PROGRESS: {
    label: "In Progress",
    variant: "warning" as const,
    icon: Clock,
  },
  PENDING: {
    label: "Pending",
    variant: "secondary" as const,
    icon: Clock,
  },
  CANCELLED: {
    label: "Cancelled",
    variant: "destructive" as const,
    icon: CrossIcon,
  },
};

export const TaskCard = ({
  id,
  orderId,
  type,
  status,
  onAction,
  onClose,
  onCancelTask,
  isPending,
}: TaskCardProps) => {
  const typeConfig = taskTypeConfig[type] || {
    icon: Calendar,
    label: type,
  };
  const statusCfg = statusConfig[status] || {
    icon: Calendar,
    label: status,
    variant: "secondary" as const,
  };
  const TypeIcon = typeConfig?.icon;
  const StatusIcon = statusCfg?.icon;

  return (
    <Card className="p-3 sm:p-5 bg-card hover:shadow-md transition-all duration-300 border border-border hover:border-primary/30 group rounded-xl flex flex-col justify-between">
      {/* Top Section: Badge + Status Icon */}
      <div className="flex items-center justify-between mb-3">
        {/* Status Badge on top-left */}

        <div
          className={`p-2 rounded-lg ${
            status === "COMPLETED"
              ? "bg-success/15 text-success"
              : status === "IN_PROGRESS"
              ? "bg-warning/15 text-warning"
              : status === "PENDING"
              ? "bg-red-100 text-red-500"
              : "bg-destructive/15 text-destructive"
          }`}
        >
          <StatusIcon className="h-5 w-5" />
        </div>

        <div className="text-[14px] text-muted-foreground mt-1">
          <p>
            Task ID: <span className="text-foreground font-semibold">{id}</span>
          </p>
          {orderId && (
            <p>
              Order ID:{" "}
              <span className="text-foreground font-semibold">{orderId}</span>
            </p>
          )}
        </div>

        {/* Status Icon on right */}

        <Badge
          variant={statusCfg.variant}
          className={`px-2 py-0.5 font-semibold text-xs rounded-full ${
            status === "COMPLETED"
              ? "bg-success/20 text-success"
              : status === "IN_PROGRESS"
              ? "bg-warning/20 text-warning"
              : status === "PENDING"
              ? "bg-red-100 text-red-500"
              : "bg-destructive/20 text-destructive"
          }`}
        >
          <StatusIcon className="h-3 w-3 mr-1 " />
          {statusCfg.label}
        </Badge>
      </div>

      {/* Middle Section: Task Type */}
      <div className="flex items-center justify-between  text-sm sm:text-base font-medium text-foreground my-2 ">
        <div className="flex items-center justify-center">
          <TypeIcon className="h-8 w-8 mr-2 text-[14px]" />
          <span>{typeConfig?.label}</span>
        </div>
        <div className="mt-3 flex items-center justify-center gap-3">
          {/* Cancel or Close - align left, smaller size */}
          {status === "PENDING" && (
            <Button
              variant="destructive"
              size="sm"
              disabled={isPending}
              className="w-fit px-3 py-1 text-xs w-[80px]"
              onClick={(e) => {
                e.stopPropagation();
                onCancelTask(id);
              }}
            >
              Cancel
            </Button>
          )}

          {status === "IN_PROGRESS" && (
            <Button
              variant="destructive"
              size="sm"
              disabled={isPending}
              className="w-fit px-3 py-1 text-xs w-[80px]"
              onClick={(e) => {
                e.stopPropagation();
                onClose?.();
              }}
            >
              Close
            </Button>
          )}
          {(type === TaskTypeEnum.DUE_COLLECTION || status !== "COMPLETED") && (
            <Button
              variant="success"
              size="sm"
              disabled={isPending}
              className="w-fit px-3 py-1 text-xs w-[80px]"
              onClick={(e) => {
                e.stopPropagation();
                onAction();
              }}
            >
              {type === TaskTypeEnum.DUE_COLLECTION
                ? "Collect Cash"
                : "Start Task"}
            </Button>
          )}
        </div>
      </div>
    </Card>
  );
};
