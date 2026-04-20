"use client";

import { useState } from "react";
import Link from "next/link";
import { Eye, EyeOff } from "lucide-react";
import { signup } from "@/lib/api";
import { setAuth } from "@/lib/auth";

const inputBase: React.CSSProperties = {
    background: "rgba(255,255,255,0.04)",
    border: "1px solid rgba(255,255,255,0.08)",
    width: "100%",
    borderRadius: 12,
    padding: "12px 16px",
    fontSize: 15,
    color: "#fff",
    outline: "none",
};

const focusBorder = "1px solid rgba(255,153,51,0.5)";
const blurBorder  = "1px solid rgba(255,255,255,0.08)";

export default function RegisterPage() {
    const [form, setForm] = useState({
        firstName: "", lastName: "", username: "", email: "", password: "",
    });
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError]   = useState("");
    const [loading, setLoading] = useState(false);

    const set = (field: keyof typeof form) =>
        (e: React.ChangeEvent<HTMLInputElement>) => {
            setForm((f) => ({ ...f, [field]: e.target.value }));
            setError("");
        };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        const { firstName, lastName, username, email, password } = form;
        if (!firstName || !lastName || !username || !email || !password) {
            setError("Please fill in all fields.");
            return;
        }
        if (password.length < 8) {
            setError("Password must be at least 8 characters.");
            return;
        }
        setLoading(true);
        try {
            const res = await signup({ firstName, lastName, username, email, password });
            setAuth(res.token, res.user);
            window.location.href = "/"; // full reload so nav picks up auth state
        } catch {
            setError("Registration failed. Username or email may already be taken.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div
            className="min-h-screen flex items-center justify-center px-4 py-20"
            style={{
                background: `
                    radial-gradient(ellipse 70% 60% at 20% 30%, rgba(255,153,51,0.10) 0%, transparent 70%),
                    radial-gradient(ellipse 55% 50% at 85% 10%, rgba(124,58,237,0.07) 0%, transparent 65%),
                    #080A0C
                `,
            }}
        >
            <div style={{ width: "100%", maxWidth: 480 }}>

                {/* Logo */}
                <div className="text-center mb-10">
                    <Link href="/">
                        <span className="font-black text-white" style={{ fontSize: 32, letterSpacing: "-0.04em" }}>
                            Indo<span style={{ color: "#FF9933" }}>DB</span>
                        </span>
                    </Link>
                    <p className="mt-3" style={{ fontSize: 15, color: "#737373" }}>Create your account</p>
                </div>

                {/* Card */}
                <div className="rounded-2xl p-8" style={{ background: "#0d0f11", border: "1px solid rgba(255,255,255,0.08)", boxShadow: "0 0 60px rgba(0,0,0,0.5)" }}>
                    <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: 20 }}>

                        {/* First + Last name */}
                        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>
                            <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
                                <label style={{ fontSize: 13, color: "#a3a3a3", fontWeight: 500 }}>First Name</label>
                                <input
                                    type="text" autoComplete="given-name" placeholder="Arjun"
                                    value={form.firstName} onChange={set("firstName")}
                                    style={inputBase}
                                    onFocus={(e) => (e.target.style.border = focusBorder)}
                                    onBlur={(e)  => (e.target.style.border = blurBorder)}
                                />
                            </div>
                            <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
                                <label style={{ fontSize: 13, color: "#a3a3a3", fontWeight: 500 }}>Last Name</label>
                                <input
                                    type="text" autoComplete="family-name" placeholder="Sharma"
                                    value={form.lastName} onChange={set("lastName")}
                                    style={inputBase}
                                    onFocus={(e) => (e.target.style.border = focusBorder)}
                                    onBlur={(e)  => (e.target.style.border = blurBorder)}
                                />
                            </div>
                        </div>

                        {/* Username */}
                        <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
                            <label style={{ fontSize: 13, color: "#a3a3a3", fontWeight: 500 }}>Username</label>
                            <input
                                type="text" autoComplete="username" placeholder="arjun_sharma"
                                value={form.username} onChange={set("username")}
                                style={inputBase}
                                onFocus={(e) => (e.target.style.border = focusBorder)}
                                onBlur={(e)  => (e.target.style.border = blurBorder)}
                            />
                        </div>

                        {/* Email */}
                        <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
                            <label style={{ fontSize: 13, color: "#a3a3a3", fontWeight: 500 }}>Email</label>
                            <input
                                type="email" autoComplete="email" placeholder="you@example.com"
                                value={form.email} onChange={set("email")}
                                style={inputBase}
                                onFocus={(e) => (e.target.style.border = focusBorder)}
                                onBlur={(e)  => (e.target.style.border = blurBorder)}
                            />
                        </div>

                        {/* Password */}
                        <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
                            <label style={{ fontSize: 13, color: "#a3a3a3", fontWeight: 500 }}>Password</label>
                            <div style={{ position: "relative" }}>
                                <input
                                    type={showPassword ? "text" : "password"}
                                    autoComplete="new-password" placeholder="Min. 8 characters"
                                    value={form.password} onChange={set("password")}
                                    style={{ ...inputBase, paddingRight: 48 }}
                                    onFocus={(e) => (e.target.style.border = focusBorder)}
                                    onBlur={(e)  => (e.target.style.border = blurBorder)}
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword((v) => !v)}
                                    className="inline-flex h-8 w-8 items-center justify-center rounded-md transition-colors hover:bg-white/5"
                                    style={{ position: "absolute", right: 12, top: "50%", transform: "translateY(-50%)", color: "#525252", background: "none", border: "none", cursor: "pointer" }}
                                >
                                    {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                                </button>
                            </div>
                        </div>

                        {/* Error */}
                        {error && <p style={{ fontSize: 13, color: "#ef4444", margin: 0 }}>{error}</p>}

                        {/* Submit */}
                        <button
                            type="submit" disabled={loading}
                            style={{ background: "#FF9933", color: "#000", fontWeight: 700, fontSize: 15, border: "none", borderRadius: 12, padding: "14px 0", cursor: loading ? "not-allowed" : "pointer", opacity: loading ? 0.6 : 1, marginTop: 4 }}
                        >
                            {loading ? "Creating account…" : "Create Account"}
                        </button>
                    </form>
                </div>

                <p className="mt-6 text-center" style={{ fontSize: 14, color: "#525252" }}>
                    Already have an account?{" "}
                    <Link href="/login" style={{ color: "#FF9933", fontWeight: 600 }}>Sign in</Link>
                </p>
            </div>
        </div>
    );
}
