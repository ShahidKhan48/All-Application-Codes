import { formatDate } from "date-fns";
export const dateFormat = (date: string | number | Date, formatStr: string) => {
  try {
    return formatDate(date, formatStr);
  } catch {
    return "";
  }
};
