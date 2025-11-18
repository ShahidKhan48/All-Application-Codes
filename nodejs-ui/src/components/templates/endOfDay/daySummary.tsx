import { Card } from "@/components/ui/card";
import { useTaskStore } from "@/hooks/useTasks";
import { Calendar } from "lucide-react";
import { useMemo } from "react";

const DaySummary = () => {
  const dashbordData = useTaskStore((state) => state.dashbordData);
  const overallCollectionData = useTaskStore(
    (state) => state.overallCollectionData
  );

  const completedTasksCount = useMemo(
    () =>
      dashbordData?.agent_task_response?.reduce((count, agentTask) => {
        const tasksWithStatus = agentTask.tasks.filter(
          (task) => task.status === "COMPLETED"
        );
        return count + tasksWithStatus.length;
      }, 0),
    [dashbordData]
  );

  const allTasksCount = useMemo(
    () =>
      dashbordData?.agent_task_response?.reduce((count, agentTask) => {
        return count + agentTask.tasks.length;
      }, 0),
    [dashbordData]
  );

  return (
    <div className="w-full max-w-2xl mx-auto p-3 sm:p-6">
      <div className="bg-card rounded-xl sm:rounded-2xl shadow-dashboard-lg border border-border/50 overflow-hidden">
        {/* Header */}
        <div className="px-4 py-3 sm:px-6 sm:py-5 border-b border-border/30">
          <div className="flex items-center gap-2 sm:gap-3">
            <div className="p-1.5 sm:p-2 rounded-lg bg-primary/10">
              <Calendar className="w-4 h-4 sm:w-5 sm:h-5 text-primary" />
            </div>
            <h1 className="text-lg sm:text-xl lg:text-2xl font-semibold text-foreground">
              Day Summary
            </h1>
          </div>
        </div>

        {/* Content */}
        <div className="p-3 sm:p-6">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-6">
            {/* Tasks Card */}
            <Card className="relative overflow-hidden text-center border-0 bg-dashboard-task-bg shadow-dashboard p-4 sm:p-6 transition-all duration-200 hover:shadow-dashboard-lg hover:scale-[1.02]">
              <div className="space-y-2 sm:space-y-3">
                <div className="text-2xl sm:text-3xl lg:text-4xl font-bold text-dashboard-task-accent leading-none">
                  {completedTasksCount}/{allTasksCount}
                </div>
                <div className="space-y-0.5 sm:space-y-1">
                  <div className="text-xs sm:text-sm lg:text-base font-medium text-dashboard-task-text">
                    Tasks
                  </div>
                  <div className="text-xs sm:text-sm lg:text-base font-medium text-dashboard-task-text">
                    Completed
                  </div>
                </div>
              </div>

              {/* Progress indicator */}
              <div className="absolute bottom-0 left-0 right-0 h-1 bg-dashboard-task-bg">
                <div
                  className="h-full bg-dashboard-task-accent transition-all duration-500"
                  style={{
                    width: `${(completedTasksCount / allTasksCount) * 100}%`,
                  }}
                />
              </div>
            </Card>

            {/* Amount Card */}
            <Card className="relative overflow-hidden  text-center border-0 bg-dashboard-amount-bg shadow-dashboard p-4 sm:p-6 transition-all duration-200 hover:shadow-dashboard-lg hover:scale-[1.02]">
              <div className="space-y-2 sm:space-y-3">
                <div className="text-lg sm:text-2xl lg:text-3xl xl:text-4xl font-bold text-dashboard-amount-accent leading-none break-words">
                  ₹
                  {
                    overallCollectionData?.outstanding_amount
                      ?.total_outstanding_amount
                  }
                </div>
                <div className="space-y-0.5 sm:space-y-1">
                  <div className="text-xs sm:text-sm lg:text-base font-medium text-dashboard-amount-text">
                    Total Outstanding
                  </div>
                  <div className="text-xs sm:text-sm lg:text-base font-medium text-dashboard-amount-text">
                    Amount
                  </div>
                </div>
              </div>

              {/* Decorative element */}
              <div className="absolute top-0 right-0 w-12 h-12 sm:w-16 sm:h-16 bg-dashboard-amount-accent/10 rounded-bl-full" />
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
};
export default DaySummary;
