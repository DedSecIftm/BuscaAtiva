import type { ReactNode } from "react";
import { Navigate } from "react-router";

type ProtectedRouteProps = {
    children: ReactNode,
    token: string | null
}

export default function ProtectedRoute( {children, token}: ProtectedRouteProps) {
    if (!token) {
        return <Navigate to={"/login"} replace />;
    }

    return children;
}