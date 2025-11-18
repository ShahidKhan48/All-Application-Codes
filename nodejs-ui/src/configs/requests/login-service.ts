import { apiClient } from "../api-client";
import { paths } from "../paths";

export interface OtpRequestPayload {
  input: {
    identity: string;
    otpRequestType: "NINJA_APP_LOGIN" | string; // restrict or keep generic
    identityType: "PHONE_NUMBER" | string; // restrict or keep generic
    captchaText: string;
  };
}

export interface OtpVerifyPayload {
  input: {
    identity: string;
    otp: string;
    identityType: "PHONE_NUMBER";
    otpRequestType: "NINJA_APP_LOGIN";
  };
}

interface IfetchUserRolesAndPrivilegesPayload {
  input: {
    realmId: string;
    userId: string;
  };
}

export const sendOtpAsync = async (payload: OtpRequestPayload) => {
  try {
    const response = await apiClient.post(paths.sendOTP(), payload);
    return response?.data;
  } catch (e) {
    throw e;
  }
};

export const verifyOtpAsync = async (payload: OtpVerifyPayload) => {
  try {
    const response = await apiClient.post(paths.verifyOTP(), payload);
    return response?.data;
  } catch (e) {
    throw e;
  }
};

export const fetchUserRolesAndPrivilegesAsync = async (
  payload: IfetchUserRolesAndPrivilegesPayload
) => {
  try {
    const response = await apiClient.post(
      paths.fetchUserRolesAndPrivileges(
        payload.input.realmId,
        payload.input.userId
      ),
      payload
    );
    return response;
  } catch (e) {
    throw e;
  }
};
