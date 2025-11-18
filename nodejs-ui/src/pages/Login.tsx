import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { Smartphone, Shield, ArrowRight, Loader2 } from "lucide-react";
import { useMutation } from "@tanstack/react-query";
import { sendOtpAsync, verifyOtpAsync } from "@/configs/requests/login-service";
import { useToast } from "@/hooks/use-toast";
import { getAuthData } from "@/lib/storage";

const Login = () => {
  const navigate = useNavigate();
  const authData = getAuthData()
  const [step, setStep] = useState<"phone" | "captcha" | "otp">("phone");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [captchaImage, setCaptchaImage] = useState<string | null>(null);
  const [captchaText, setCaptchaText] = useState("");
  const [otp, setOtp] = useState("");
  const [countdown, setCountdown] = useState(0);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const OTP_DIGIT = 6;
  const { toast } = useToast();

  const { mutateAsync: sendOtpMutate } = useMutation({
    mutationKey: ["sendOtpAsync"],
    mutationFn: sendOtpAsync,
    onError: (e: any) => {
      if (e?.data?.responseCode === "CAPTCHA_REQUIRED") return;
      toast({
        title: "Failed",
        description:
          e?.message ||
          e?.errorMessage ||
          "Something went wrong while sending OTP",
        variant: "destructive",
      });
    },
  });

  const { mutateAsync: verifyOtpMutate } = useMutation({
    mutationKey: ["verifyOtpAsync"],
    mutationFn: verifyOtpAsync,
    onError: (e: any) => {
      toast({
        title: "Failed",
        description:
          e?.data?.data?.message ||
          e?.errorMessage ||
          "OTP verification failed",
        variant: "destructive",
      });
    },
  });

  const handlePhoneSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (phoneNumber.length !== 10) {
      setError("Please enter a valid 10-digit phone number");
      return;
    }
    setError("");
    sendOtpMutate({
      input: {
        identity: phoneNumber,
        otpRequestType: "NINJA_APP_LOGIN",
        identityType: "PHONE_NUMBER",
        captchaText: null,
      },
    })
      .then((res) => {
        setStep("otp");
        setCountdown(30);

        // Start countdown
        const timer = setInterval(() => {
          setCountdown((prev) => {
            if (prev <= 1) {
              clearInterval(timer);
              return 0;
            }
            return prev - 1;
          });
        }, 1000);
      })
      .catch((err) => {
        if (
          err?.data?.responseCode === "CAPTCHA_REQUIRED" &&
          err?.data?.data?.captchaImage
        ) {
          setCaptchaImage(
            `data:image/jpeg;base64,${err.data.data.captchaImage}`
          );
          setStep("captcha");
        }
      });
  };

  const handleCaptchaSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!captchaText) {
      setError("Please enter captcha");
      return;
    }

    setError("");
    setLoading(true);

    sendOtpMutate({
      input: {
        identity: phoneNumber,
        otpRequestType: "NINJA_APP_LOGIN",
        identityType: "PHONE_NUMBER",
        captchaText: captchaText,
      },
    })
      .then((res) => {
        setStep("otp");
        setCountdown(30);

        // Start countdown
        const timer = setInterval(() => {
          setCountdown((prev) => {
            if (prev <= 1) {
              clearInterval(timer);
              return 0;
            }
            return prev - 1;
          });
        }, 1000);
      })
      .finally(() => setLoading(false));
  };

  const handleOtpSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // if (otp === '1234') {
    //   navigate('/dashboard');
    // } else {
    //   setError('Invalid OTP. Please try again.');
    // }
    verifyOtpMutate({
      input: {
        identity: phoneNumber,
        otp: otp,
        identityType: "PHONE_NUMBER",
        otpRequestType: "NINJA_APP_LOGIN",
      },
    }).then((res) => {
      console.log(res, "******");
      localStorage.setItem("authData", JSON.stringify(res));
      navigate("/select-account");
    });
  };

  const handleResendOtp = (e) => {
    handlePhoneSubmit(e);
    setCountdown(30);
    setError("");
    const timer = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
  };

 

  return (
    <div className="min-h-screen bg-gradient-primary flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-white rounded-full mb-4 shadow-lg">
            <Shield className="w-8 h-8 text-primary" />
          </div>
          <h1 className="text-2xl font-bold text-white mb-2">
            Sales Agent Portal
          </h1>
          <p className="text-white/80">Secure login for field agents</p>
        </div>

        <Card className="shadow-xl border-0">
          <CardHeader className="text-center">
            <CardTitle className="flex items-center justify-center gap-2">
              <Smartphone className="w-5 h-5 text-primary" />
              {step === "phone"
                ? "Enter Phone Number"
                : step === "captcha"
                ? "Verify Captcha"
                : "Verify OTP"}
            </CardTitle>
            <CardDescription>
              {step === "phone"
                ? "Enter your registered phone number to receive OTP"
                : step === "captcha"
                ? "Please verify the captcha below"
                : `OTP sent to +91 ${phoneNumber}`}
            </CardDescription>
          </CardHeader>

          <CardContent className="space-y-6">
            {/* Phone Step */}
            {step === "phone" && (
              <form onSubmit={handlePhoneSubmit} className="space-y-4">
                <div>
                  <Label htmlFor="phone">Phone Number</Label>
                  <div className="flex mt-1">
                    <span className="inline-flex items-center px-3 rounded-l-lg border border-r-0 border-input bg-muted text-muted-foreground text-sm">
                      +91
                    </span>
                    <Input
                      id="phone"
                      type="tel"
                      placeholder="Enter 10-digit number"
                      value={phoneNumber}
                      onChange={(e) =>
                        setPhoneNumber(
                          e.target.value.replace(/\D/g, "").slice(0, 10)
                        )
                      }
                      className="rounded-l-none"
                      maxLength={10}
                    />
                  </div>
                </div>
                {error && <p className="text-danger text-sm">{error}</p>}
                <Button type="submit" size="mobile" className="w-full">
                  Send OTP
                  <ArrowRight className="w-4 h-4 ml-2" />
                </Button>
              </form>
            )}

            {/* Captcha Step */}
            {step === "captcha" && (
              <form onSubmit={handleCaptchaSubmit} className="space-y-4">
                {captchaImage && (
                  <div className="flex justify-center">
                    <img
                      src={captchaImage}
                      alt="Captcha"
                      className="border rounded"
                    />
                  </div>
                )}
                <div>
                  <Label htmlFor="captcha">Enter Captcha</Label>
                  <Input
                    id="captcha"
                    placeholder="Enter the text shown above"
                    value={captchaText}
                    onChange={(e) => setCaptchaText(e.target.value)}
                  />
                </div>
                {error && <p className="text-danger text-sm">{error}</p>}
                <Button
                  type="submit"
                  className="w-full"
                  disabled={loading}
                  size="mobile"
                >
                  {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                  Verify & Send OTP
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  className="w-full"
                  onClick={() => setStep("phone")}
                >
                  Back
                </Button>
              </form>
            )}

            {/* OTP Step */}
            {step === "otp" && (
              <form onSubmit={handleOtpSubmit} className="space-y-4">
                <div>
                  <Label htmlFor="otp">Enter OTP</Label>
                  <Input
                    id="otp"
                    type="text"
                    placeholder={`Enter ${OTP_DIGIT}-digit OTP`}
                    value={otp}
                    onChange={(e) =>
                      setOtp(
                        e.target.value.replace(/\D/g, "").slice(0, OTP_DIGIT)
                      )
                    }
                    className="text-center text-lg tracking-wider"
                    maxLength={OTP_DIGIT}
                  />
                  <p className="text-xs text-muted-foreground mt-1">
                    Use OTP:{" "}
                    <span className="font-mono font-semibold">123456</span> for
                    demo
                  </p>
                </div>
                {error && <p className="text-danger text-sm">{error}</p>}
                <Button type="submit" size="mobile" className="w-full">
                  Verify & Login
                  <ArrowRight className="w-4 h-4 ml-2" />
                </Button>
                <div className="text-center">
                  {countdown > 0 ? (
                    <p className="text-sm text-muted-foreground">
                      Resend OTP in {countdown}s
                    </p>
                  ) : (
                    <button
                      type="button"
                      onClick={(e) => handleResendOtp(e)}
                      className="text-sm text-primary hover:underline"
                    >
                      Resend OTP
                    </button>
                  )}
                </div>
              </form>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default Login;
