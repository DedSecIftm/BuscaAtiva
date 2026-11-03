import { Box, Button, Container, Stack, TextField } from "@mui/material";
import { useState, type SetStateAction } from "react";
import useToken, { type UserToken } from "../../regras/useToken";

type LoginProps = {
    setToken: (userToken: UserToken) => void;
}

type Credentials = {
    username: string,
    password: string
}

const BACKEND_URL = "http://localhost:8080"

function requestLogin(credentials: Credentials, setToken: any) {
    const body = JSON.stringify(credentials);

    console.log("Valor do body:", body);

    fetch(BACKEND_URL + "/token", {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: body
    })
        .then(response => response.json())
        .then(data => setToken(data))
        .catch(error => console.error("Error:", error));
}

export default function Login({ setToken }: LoginProps) {
    const [username, setUsername] = useState<string>('');
    const [password, setPassword] = useState<string>('');

    return (
        <Container maxWidth="md" sx={{ py: 4 }}>
            <Box>
                <h1>Faça login</h1>
                <Stack spacing={2}>
                    <TextField onChange={e => setUsername(e.target.value)} id="username" label="Usuário" variant="outlined" />
                    <TextField onChange={e => setPassword(e.target.value)} id="password" label="Senha" type="password" variant="outlined" />
                    <Button onClick={() => requestLogin({ username, password }, setToken) } variant="contained">Entrar</Button>
                </Stack>
            </Box>
        </Container>
    );
}