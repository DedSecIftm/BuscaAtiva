import { useState } from "react";
import type { FormEvent } from "react";
import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Container from "@mui/material/Container";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import { entrar, ErroLogin } from "../../autenticacao/api";
import type { ErrosDoLogin } from "../../autenticacao/api";
import type { UserToken } from "../../autenticacao/sessao";

type LoginProps = {
    setToken: (userToken: UserToken) => void;
    sessaoExpirada?: boolean;
}

export default function Login({ setToken, sessaoExpirada = false }: LoginProps) {
    const [email, setEmail] = useState<string>('');
    const [password, setPassword] = useState<string>('');
    const [enviando, setEnviando] = useState(false);
    const [erro, setErro] = useState<string | null>(null);
    const [errosDosCampos, setErrosDosCampos] = useState<ErrosDoLogin>({});

    async function enviar(evento: FormEvent) {
        evento.preventDefault();
        setEnviando(true);
        setErro(null);
        setErrosDosCampos({});
        try {
            setToken(await entrar({ email, password }));
        } catch (e) {
            if (e instanceof ErroLogin) {
                setErro(e.message);
                setErrosDosCampos(e.erros);
            } else {
                setErro('Ocorreu um erro inesperado. Tente novamente.');
            }
            setEnviando(false);
        }
    }

    return (
        <Container maxWidth="sm" sx={{ py: 4 }}>
            <Box component="form" noValidate onSubmit={enviar}>
                <Typography variant="h4" component="h1" gutterBottom sx={{ fontWeight: 700 }}>
                    Faça login
                </Typography>
                <Stack spacing={2}>
                    {sessaoExpirada && !erro && <Alert severity="warning">Sua sessão expirou. Entre novamente para continuar.</Alert>}
                    {erro && <Alert severity="error">{erro}</Alert>}
                    <TextField
                        id="email"
                        label="E-mail"
                        type="email"
                        autoComplete="username"
                        value={email}
                        onChange={e => setEmail(e.target.value)}
                        error={Boolean(errosDosCampos.email)}
                        helperText={errosDosCampos.email}
                    />
                    <TextField
                        id="password"
                        label="Senha"
                        type="password"
                        autoComplete="current-password"
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                        error={Boolean(errosDosCampos.password)}
                        helperText={errosDosCampos.password}
                    />
                    <Button type="submit" variant="contained" size="large" disabled={enviando}>
                        {enviando ? 'Entrando...' : 'Entrar'}
                    </Button>
                </Stack>
            </Box>
        </Container>
    );
}
