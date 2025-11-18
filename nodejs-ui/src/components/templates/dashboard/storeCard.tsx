import React from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { PhoneCall, MapPin } from "lucide-react";
import { cn } from "@/lib/utils";
import MapButton from "@/components/molecules/map-button";

interface Task {
  task_type: string;
  status: string;
}

interface StoreItem {
  store_name: string;
  contact_number?: string;
  distance?: number;
  lat?: number;
  lng?: number;
  store_status: string;
  tasks: Task[];
}

interface CompactStoreCardProps {
  item: StoreItem;
  index: number;
  isUpdating: boolean;
  isClosingTask: boolean;
  onStoreClick: (item: StoreItem) => void;
  onCompleteStoreVisit: (item: StoreItem) => void;
  onEndStore: (tasks: Task[]) => void;
}

const getStatusColor = (status: string) => {
  switch (status) {
    case "COMPLETED":
      return "bg-success/20 text-success";
    case "CANCELLED":
      return "bg-destructive/20 text-destructive";
    case "CLOSED":
      return "bg-secondary/20 text-secondary";
    default:
      return "bg-muted text-muted-foreground";
  }
};

export const StoreCard: React.FC<CompactStoreCardProps> = ({
  item,
  index,
  isUpdating,
  isClosingTask,
  onStoreClick,
  onCompleteStoreVisit,
  onEndStore,
}) => {
  const tasksCount =
    item?.tasks?.filter(
      (ch) =>
        ch.task_type !== "STORE_VISIT" &&
        (ch.status === "PENDING" || ch.status === "IN_PROGRESS")
    )?.length || 0;

  return (
    <Card
      className="cursor-pointer hover:shadow-md transition-all duration-200 border-card-compact-border bg-card-compact"
      onClick={(e) => {
        e.stopPropagation();
        if (item?.store_status === "COMPLETED") {
          return;
        }
        onStoreClick(item);
      }}
    >
      <CardContent className="p-3">
        <div className="text-xs relative font-light bg-gradient-primary text-[white] p-1 w-[55%] left-[-10px] top-[-20px]">
          {new Date().toLocaleDateString("en-IN", {
            weekday: "long",
            year: "numeric",
            month: "long",
            day: "numeric",
          })}
        </div>
        <div className="flex items-start justify-between gap-3">
          {/* Left side - Store details */}
          <div className="flex-1 space-y-1">
            <h3 className="font-semibold text-base text-primary-dark line-clamp-2 leading-tight">
              {item?.store_name}
            </h3>

            <div className="flex flex-col gap-1">
              {item.contact_number && (
                <div className="flex items-center gap-1 text-xs text-muted-foreground">
                  <PhoneCall className="w-3 h-3 shrink-0" />
                  <span>+91-{item.contact_number}</span>
                </div>
              )}

              {item.distance && (
                <div className="flex items-center gap-1 text-xs text-muted-foreground">
                  <MapPin className="w-3 h-3 shrink-0" />
                  <span>{item.distance}</span>
                </div>
              )}
            </div>
          </div>

          {/* Right side - Map and Tasks */}
          <div className="flex flex-col items-center gap-1 shrink-0">
            <MapButton lat={item?.lat} lng={item?.lng} />
            <div className="text-xs font-medium text-primary text-center">
              {tasksCount} Tasks
            </div>
          </div>
        </div>

        {/* Bottom row - Status and Action button */}
        <div className="flex justify-between items-center mt-2 pt-2 border-t border-card-compact-border">
          <Badge
            className={cn(
              "text-xs px-2 py-0.5",
              getStatusColor(item.store_status)
            )}
          >
            {(item.store_status || "").toUpperCase()}
          </Badge>

          <div className="shrink-0">
            {item?.tasks?.find((task) => task?.task_type === "STORE_VISIT")
              ?.status === "PENDING" ? (
              <Button
                variant="success"
                size="sm"
                disabled={isUpdating}
                className="h-7 px-3 text-xs"
                onClick={(e) => {
                  e.stopPropagation();
                  onCompleteStoreVisit(item);
                }}
              >
                Start
              </Button>
            ) : item?.store_status !== "COMPLETED" ? (
              <Button
                variant="destructive"
                size="sm"
                disabled={isClosingTask}
                className="h-7 px-3 text-xs"
                onClick={(e) => {
                  e.stopPropagation();
                  onEndStore(item.tasks);
                }}
              >
                End Store
              </Button>
            ) : null}
          </div>
        </div>
      </CardContent>
    </Card>
  );
};
