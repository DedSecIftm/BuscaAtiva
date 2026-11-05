import { useState } from "react";

export type UserToken = {
    token:string
}

export function getToken(): string | null {
    const tokenString = localStorage.getItem('token');
    if (!tokenString) return null;

    const userToken = JSON.parse(tokenString);
    return userToken?.token ?? null;
}

export default function useToken() {
    const [token, setToken] = useState<string | null>(getToken());

    const saveToken = (userToken: UserToken) => {
        localStorage.setItem('token', JSON.stringify(userToken));
        setToken(userToken.token);
    }

    return {
        token,
        setToken: saveToken
    }
}