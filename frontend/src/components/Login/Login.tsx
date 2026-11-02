import { Box, Button, Container, Stack, TextField } from "@mui/material";
import { useState, type SetStateAction } from "react";

type LoginProps = {
    setToken: React.Dispatch<SetStateAction<string | null>>
}

type Credentials = {
    username: string,
    password: string 
}

function setCredentials(credentials: Credentials) {
    console.log(`Credentials: username ${credentials.username} | password ${credentials.password}`);
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
                    <Button onClick={() => setCredentials({username, password})} variant="contained">Entrar</Button>
                </Stack>
            </Box>
        </Container>
    );

}