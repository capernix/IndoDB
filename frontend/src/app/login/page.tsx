"use client";

import { useState } from "react";
import Link from "next/link";
import { Eye, EyeOff } from "lucide-react";
import { login } from "@/lib/api";
import { setAuth } from "@/lib/auth";

export default function LoginPage() {
    const [form, setForm] = useState({ emailOrUsername: "", password: "" });
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setForm((f) => ({ ...f, [e.target.name]: e.target.value }));
        setError("");
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!form.emailOrUsername || !form.password) {
            setError("Please fill in all fields.");
            return;
        }
        setLoading(true);
        try {
            const res = await login(form.emailOrUsername, form.password);
            setAuth(res.token, res.user);
            window.location.href = "/"; // full reload so nav picks up auth state
        } catch {
            setError("Invalid credentials. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div
            className="min-h-screen flex items-center justify-center px-4"
            style={{
                background: `
                    radial-gradient(ellipse 70% 60% at 20% 30%, rgba(255,153,51,0.10) 0%, transparent 70%),
                    radial-gradient(ellipse 55% 50% at 85% 10%, rgba(124,58,237,0.07) 0%, transparent 65%),
                    #080A0C
                `,
            }}
        >
            <div className="w-full" style={{ maxWidth: 440 }}>

                {/* Logo */}
                <div className="text-center mb-10">
                    <Link href="/">
                        <span
                            className="font-black text-white"
                            style={{ fontSize: 32, letterSpacing: "-0.04em" }}
                        >
                            Indo<span style={{ color: "#FF9933" }}>DB</span>
                        </span>
                    </Link>
                    <p className="mt-3 text-[15px]" style={{ color: "#737373" }}>
                        Sign in to your account
                    </p>
                </div>

                {/* Card */}
                <div
                    className="rounded-2xl p-8"
                    style={{
                        background: "#0d0f11",
                        border: "1px solid rgba(255,255,255,0.08)",
                        boxShadow: "0 0 60px rgba(0,0,0,0.5)",
                    }}
                >
                    <form onSubmit={handleSubmit} className="flex flex-col gap-5">

                        {/* Email / Username */}
                        <div className="flex flex-col gap-2">
                            <label style={{ fontSize: 13, color: "#a3a3a3", fontWeight: 500 }}>
                                Email or Username
                            </label>
                            <input
                                name="emailOrUsername"
                                type="text"
                                autoComplete="username"
                                placeholder="you@example.com"
                                value={form.emailOrUsername}
                                onChange={handleChange}
                                className="w-full rounded-xl px-4 py-3 text-[15px] text-white outline-none transition-all"
                                style={{
                                    background: "rgba(255,255,255,0.04)",
                                    border: error ? "1px solid rgba(239,68,68,0.5)" : "1px solid rgba(255,255,255,0.08)",
                                }}
                                onFocus={(e) => (e.target.style.border = "1px solid rgba(255,153,51,0.5)")}
                                onBlur={(e) => (e.target.style.border = error ? "1px solid rgba(239,68,68,0.5)" : "1px solid rgba(255,255,255,0.08)")}
                            />
                        </div>

                        {/* Password */}
                        <div className="flex flex-col gap-2">
                            <label style={{ fontSize: 13, color: "#a3a3a3", fontWeight: 500 }}>
                                Password
                            </label>
                            <div className="relative">
                                <input
                                    name="password"
                                    type={showPassword ? "text" : "password"}
                                    autoComplete="current-password"
                                    placeholder="••••••••"
                                    value={form.password}
                                    onChange={handleChange}
                                    className="w-full rounded-xl px-4 py-3 pr-12 text-[15px] text-white outline-none transition-all"
                                    style={{
                                        background: "rgba(255,255,255,0.04)",
                                        border: error ? "1px solid rgba(239,68,68,0.5)" : "1px solid rgba(255,255,255,0.08)",
                                    }}
                                    onFocus={(e) => (e.target.style.border = "1px solid rgba(255,153,51,0.5)")}
                                    onBlur={(e) => (e.target.style.border = error ? "1px solid rgba(239,68,68,0.5)" : "1px solid rgba(255,255,255,0.08)")}
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword((v) => !v)}
                                    className="absolute right-3 top-1/2 inline-flex h-8 w-8 -translate-y-1/2 items-center justify-center rounded-md transition-colors hover:bg-white/5 hover:text-white"
                                    style={{ color: "#525252" }}
                                >
                                    {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                                </button>
                            </div>
                        </div>

                        {/* Error */}
                        {error && (
                            <p style={{ fontSize: 13, color: "#ef4444" }}>{error}</p>
                        )}

                        {/* Submit */}
                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full rounded-xl py-3.5 font-bold text-black transition-opacity hover:opacity-85 disabled:opacity-50"
                            style={{ background: "#FF9933", fontSize: 15, marginTop: 4 }}
                        >
                            {loading ? "Signing in…" : "Sign In"}
                        </button>
                    </form>
                </div>

                {/* Footer link */}
                <p className="mt-6 text-center" style={{ fontSize: 14, color: "#525252" }}>
                    Don&apos;t have an account?{" "}
                    <Link href="/register" className="font-semibold hover:text-white transition-colors" style={{ color: "#FF9933" }}>
                        Create one
                    </Link>
                </p>
            </div>
        </div>
    );
}
