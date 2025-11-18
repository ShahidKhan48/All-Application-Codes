import "./App.css";
import { Toaster } from "@/components/ui/toaster";
import { TooltipProvider } from "@/components/ui/tooltip";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { Button } from "@/components/ui/button"; // 👈 added

import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import TaskDetail from "./pages/TaskDetail";
import Collections from "./pages/Collections";
import EndOfDay from "./pages/EndOfDay";
import SalesOrder from "./pages/SalesOrder";
import NotFound from "./pages/NotFound";
import StoreLists from "./pages/StoreLists";
import StoreTasks from "./pages/StoreTasks";
import SelectAccount from "./pages/select-account";
import QueryProvider from "./queryProvider";
import OrderLists from "./pages/orderLists";
import ProfileIndex from "./pages/profile";
import useUpdateAgentLocation from "./hooks/useUpdateAgentLocation";

import { useEffect, useState } from "react";
import { X } from "lucide-react";

function App() {
  useUpdateAgentLocation();

  const [deferredPrompt, setDeferredPrompt] = useState<any>(null);
  const [showInstallButton, setShowInstallButton] = useState(false);

  // 👇 Listen for install prompt event
  useEffect(() => {
    const handler = (e: any) => {
      e.preventDefault();
      setDeferredPrompt(e);
      setShowInstallButton(true);
    };

    window.addEventListener("beforeinstallprompt", handler);
    return () => window.removeEventListener("beforeinstallprompt", handler);
  }, []);

  // 👇 Handle manual install button click
  const handleInstallClick = async () => {
    if (!deferredPrompt) return;

    deferredPrompt.prompt();
    const { outcome } = await deferredPrompt.userChoice;
    console.log(`User response to install: ${outcome}`);

    setDeferredPrompt(null);
    setShowInstallButton(false);
  };

  return (
    <QueryProvider>
      <TooltipProvider>
        <Toaster />

        <BrowserRouter>
          <Routes>
            <Route path="/" element={<Login />} />
            <Route path="/select-account" element={<SelectAccount />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/task/:id" element={<TaskDetail />} />
            <Route path="/collections" element={<Collections />} />
            <Route path="/end-of-day" element={<EndOfDay />} />
            <Route path="/sales-order" element={<SalesOrder />} />
            <Route path="/store-lists" element={<StoreLists />} />
            <Route path="/store-tasks/:storeId" element={<StoreTasks />} />
            <Route path="/order-lists" element={<OrderLists />} />
            <Route path="/profile" element={<ProfileIndex />} />
            <Route path="*" element={<NotFound />} />
          </Routes>

          {/* 👇 Floating install button (visible only when installable) */}
          {showInstallButton && (
            <div className="fixed inset-0 z-50 flex items-end justify-center">
              {/* Overlay */}
              <div
                className="absolute inset-0 bg-black/40"
                onClick={() => setShowInstallButton(false)}
              ></div>

              {/* Bottom sheet */}
              <div className="relative w-full max-w-md bg-white rounded-t-2xl shadow-xl p-6 animate-slide-up">
                {/* Close button */}
                <button
                  className="absolute top-4 right-4 text-gray-500 hover:text-gray-700"
                  onClick={() => setShowInstallButton(false)}
                >
                  <X size={24} />
                </button>

                {/* Install content */}
                <div className="flex flex-col items-center gap-4">
                  <h2 className="text-lg font-semibold">Agent App</h2>
                  <p className="text-center text-gray-600">
                    This app can be installed on your device for quick access!
                  </p>
                  <Button
                    onClick={handleInstallClick}
                    className="bg-blue-600 text-white w-full"
                  >
                    Install App
                  </Button>
                </div>
              </div>
            </div>
          )}
        </BrowserRouter>
      </TooltipProvider>
    </QueryProvider>
  );
}

export default App;
