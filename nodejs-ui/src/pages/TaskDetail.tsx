import React, { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import {
  ArrowLeft,
  MapPin,
  Phone,
  ShoppingCart,
  CreditCard,
  Truck,
  UserPlus,
  CheckCircle,
  Clock,
  AlertCircle,
} from "lucide-react";
import Layout from "@/components/molecules/layout";

const TaskDetail = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const task = location.state?.task;
  const [status, setStatus] = useState(task?.status || "pending");

  if (!task) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center p-4">
        <Card className="text-center">
          <CardContent className="p-8">
            <AlertCircle className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
            <h2 className="text-xl font-semibold mb-2">Task Not Found</h2>
            <p className="text-muted-foreground mb-4">
              The task you're looking for doesn't exist.
            </p>
            <Button onClick={() => navigate("/dashboard")}>
              Back to Dashboard
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  const getTaskIcon = (type: string) => {
    switch (type) {
      case "visit":
        return <ShoppingCart className="w-6 h-6" />;
      case "collection":
        return <CreditCard className="w-6 h-6" />;
      case "delivery":
        return <Truck className="w-6 h-6" />;
      case "onboarding":
        return <UserPlus className="w-6 h-6" />;
      default:
        return <Clock className="w-6 h-6" />;
    }
  };

  const getTaskVariant = (type: string) => {
    switch (type) {
      case "visit":
        return "visit";
      case "collection":
        return "collection";
      case "delivery":
        return "delivery";
      case "onboarding":
        return "onboarding";
      default:
        return "default";
    }
  };

  const handleStartTask = () => {
    setStatus("in-progress");

    // Navigate to specific task flow based on type
    switch (task.type) {
      case "visit":
        navigate("/sales-order", { state: { task, customer: task.customer } });
        break;
      case "collection":
        navigate("/collection", { state: { task, customer: task.customer } });
        break;
      case "delivery":
        navigate("/delivery", { state: { task, customer: task.customer } });
        break;
      case "onboarding":
        navigate("/onboarding", { state: { task, customer: task.customer } });
        break;
      default:
        break;
    }
  };

  const handleCompleteTask = () => {
    setStatus("completed");
    setTimeout(() => {
      navigate("/dashboard");
    }, 1000);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "completed":
        return "text-success";
      case "in-progress":
        return "text-warning";
      case "pending":
        return "text-muted-foreground";
      default:
        return "text-muted-foreground";
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "completed":
        return <CheckCircle className="w-5 h-5 text-success" />;
      case "in-progress":
        return <Clock className="w-5 h-5 text-warning" />;
      case "pending":
        return <AlertCircle className="w-5 h-5 text-muted-foreground" />;
      default:
        return <Clock className="w-5 h-5 text-muted-foreground" />;
    }
  };

  return (
    <Layout
      headerContent={
        <div className="bg-gradient-primary text-white p-4 shadow-lg">
          <div className="flex items-center gap-3 mb-4">
            <Button
              variant="ghost"
              size="icon"
              onClick={() => navigate("/dashboard")}
              className="text-white hover:bg-white/20"
            >
              <ArrowLeft className="w-5 h-5" />
            </Button>
            <h1 className="text-lg font-semibold">Task Details</h1>
          </div>
        </div>
      }
    >
      {/* Task Overview */}
      <Card>
        <CardHeader>
          <div className="flex items-center gap-3">
            <div
              className={`p-3 rounded-lg bg-${getTaskVariant(task.type)}/10`}
            >
              {getTaskIcon(task.type)}
            </div>
            <div className="flex-1">
              <div className="flex items-center gap-2 mb-1">
                <Badge variant={getTaskVariant(task.type) as any}>
                  {task.type.toUpperCase()}
                </Badge>
                <Badge
                  variant={
                    task.priority === "high" ? "destructive" : "secondary"
                  }
                >
                  {task.priority.toUpperCase()}
                </Badge>
              </div>
              <CardTitle className="text-xl">{task.customer.name}</CardTitle>
            </div>
            <div className="flex items-center gap-2">
              {getStatusIcon(status)}
              <span className={`text-sm font-medium ${getStatusColor(status)}`}>
                {status.toUpperCase()}
              </span>
            </div>
          </div>
        </CardHeader>

        <CardContent className="space-y-4">
          <div>
            <h3 className="font-semibold mb-2">Description</h3>
            <p className="text-muted-foreground">{task.description}</p>
          </div>

          {task.amount && (
            <div>
              <h3 className="font-semibold mb-2">Amount</h3>
              <p className="text-2xl font-bold text-success">
                ₹{task.amount.toLocaleString()}
              </p>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Customer Information */}
      <Card>
        <CardHeader>
          <CardTitle>Customer Information</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="flex items-center gap-3">
            <MapPin className="w-5 h-5 text-muted-foreground" />
            <div>
              <p className="font-medium">{task.customer.address}</p>
              <p className="text-sm text-muted-foreground">
                {task.customer.area}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <Phone className="w-5 h-5 text-muted-foreground" />
            <p className="font-medium">{task.customer.phone}</p>
          </div>

          {task.customer.outstandingAmount > 0 && (
            <div className="bg-warning/10 p-3 rounded-lg">
              <p className="text-sm font-medium text-warning-foreground">
                Outstanding Amount: ₹
                {task.customer.outstandingAmount.toLocaleString()}
              </p>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Actions */}
      <div className="space-y-3">
        {status === "pending" && (
          <Button size="mobile" className="w-full" onClick={handleStartTask}>
            Start Task
          </Button>
        )}

        {status === "in-progress" && (
          <Button
            size="mobile"
            variant="success"
            className="w-full"
            onClick={handleCompleteTask}
          >
            Mark as Completed
          </Button>
        )}

        {status === "completed" && (
          <div className="text-center p-4">
            <CheckCircle className="w-16 h-16 text-success mx-auto mb-2" />
            <h3 className="text-lg font-semibold text-success">
              Task Completed!
            </h3>
            <p className="text-muted-foreground">
              This task has been successfully completed.
            </p>
          </div>
        )}

        <Button
          variant="outline"
          size="mobile"
          className="w-full"
          onClick={() => navigate("/dashboard")}
        >
          Back to Dashboard
        </Button>
      </div>
    </Layout>
  );
};

export default TaskDetail;
