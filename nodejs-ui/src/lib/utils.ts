import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"
import { TaskTypeEnum } from "./dashborad-type";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}


export function getTodayDate(): string {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, "0");
  const day = String(today.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}


export const getTaskTitle = (taskType) => {
  switch (taskType) {
    case TaskTypeEnum.STORE_AUDIT:
      return "Create Order";
    case TaskTypeEnum.DUE_COLLECTION:
      return "Collection";
    case TaskTypeEnum.DELIVERY:
      return "Delivery";
    case TaskTypeEnum.STORE_VISIT:
      return "End Store Visit";
    case TaskTypeEnum.DELIVERY_VERIFICATION:
      return "Delivery Verification";
    case TaskTypeEnum.ENACH:
      return "E-Nach";
    default:
      return "Exit";
  }
};


export function getExpiresOn() {
  // Current time
  const now = new Date();

  // Add 365 days (in ms)
  const future = new Date(now.getTime() + 365 * 24 * 60 * 60 * 1000);

  // Convert to Asia/Kolkata timezone
  const options = {
    timeZone: "Asia/Kolkata",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  };

  const formatter = new Intl.DateTimeFormat("en-CA", options);
  const parts = formatter.formatToParts(future);

  // Build YYYY-MM-DD HH:mm:ss manually
  const get = (type) => parts.find((p) => p.type === type)?.value || "00";

  return `${get("year")}-${get("month")}-${get("day")} ${get("hour")}:${get("minute")}:${get("second")}`;
}


// utils/dateFormatter.js
export function formatDateTime(isoString) {
  if (!isoString) return "";

  const date = new Date(isoString);

  const options = {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
    hour12: true,
  };

  // Format: 16 Sep 2025 9:41 PM
  return date.toLocaleString("en-GB", options).replace(",", "");
}


// Example

