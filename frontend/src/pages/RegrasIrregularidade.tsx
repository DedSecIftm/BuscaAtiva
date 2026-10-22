import { useCallback, useEffect, useState } from 'react'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Chip from '@mui/material/Chip'
import CircularProgress from '@mui/material/CircularProgress'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogTitle from '@mui/material/DialogTitle'
import FormControlLabel from '@mui/material/FormControlLabel'
import IconButton from '@mui/material/IconButton'
import Paper from '@mui/material/Paper'
import Snackbar from '@mui/material/Snackbar'
import Stack from '@mui/material/Stack'
import Switch from '@mui/material/Switch'
import Tooltip from '@mui/material/Tooltip'
import Typography from '@mui/material/Typography'
import AddIcon from '@mui/icons-material/Add'
import DeleteOutlinedIcon from '@mui/icons-material/DeleteOutlined'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import FormularioRegra from '../components/FormularioRegra.tsx'
import { atualizarRegra, criarRegra, excluirRegra, listarRegras } from '../regras/api.ts'
import { descreverRegra } from '../regras/tipos.ts'
import type { DadosRegra, Regra } from '../regras/tipos.ts'

// null = formulário fechado; 'nova' = criando; Regra = editando.
type Edicao = null | 'nova' | Regra

// Período sugerido no formulário ao editar uma regra de faltas seguidas, que não tem período.
const PERIODO_PADRAO = 10

function mensagemDe(erro: unknown): string {
  return erro instanceof Error ? erro.message : 'Ocorreu um erro inesperado.'
}

function RegrasIrregularidade() {
  const [regras, setRegras] = useState<Regra[]>([])
  const [carregando, setCarregando] = useState(true)
  const [erroAoCarregar, setErroAoCarregar] = useState<string | null>(null)
  const [edicao, setEdicao] = useState<Edicao>(null)
  const [paraExcluir, setParaExcluir] = useState<Regra | null>(null)
  const [excluindo, setExcluindo] = useState(false)
  // Regras com o switch aguardando resposta do servidor.
  const [alternando, setAlternando] = useState<Set<string>>(new Set())
  const [aviso, setAviso] = useState<string | null>(null)

  const buscarRegras = useCallback(() => {
    listarRegras()
      .then(setRegras)
      .catch((erro: unknown) => setErroAoCarregar(mensagemDe(erro)))
      .finally(() => setCarregando(false))
  }, [])

  useEffect(buscarRegras, [buscarRegras])

  function tentarNovamente() {
    setCarregando(true)
    setErroAoCarregar(null)
    buscarRegras()
  }

  function substituir(regra: Regra) {
    setRegras((atuais) => atuais.map((r) => (r.id === regra.id ? regra : r)))
  }

  // Erros são tratados pelo formulário, que continua aberto para correção.
  async function salvar(dados: DadosRegra) {
    if (edicao === 'nova') {
      const criada = await criarRegra(dados)
      setRegras((atuais) => [...atuais, criada])
      setAviso('Regra criada.')
    } else if (edicao) {
      substituir(await atualizarRegra(edicao.id, { ...dados, ativa: edicao.ativa }))
      setAviso('Regra atualizada.')
    }
    setEdicao(null)
  }

  async function alternarAtiva(regra: Regra) {
    setAlternando((atuais) => new Set(atuais).add(regra.id))
    try {
      substituir(await atualizarRegra(regra.id, { ...regra, ativa: !regra.ativa }))
    } catch (erro) {
      setAviso(`Não foi possível alterar a regra. ${mensagemDe(erro)}`)
    } finally {
      setAlternando((atuais) => {
        const restantes = new Set(atuais)
        restantes.delete(regra.id)
        return restantes
      })
    }
  }

  async function excluir() {
    if (!paraExcluir) return
    setExcluindo(true)
    try {
      await excluirRegra(paraExcluir.id)
      setRegras((atuais) => atuais.filter((r) => r.id !== paraExcluir.id))
      setAviso('Regra excluída.')
      setParaExcluir(null)
    } catch (erro) {
      setAviso(`Não foi possível excluir a regra. ${mensagemDe(erro)}`)
    } finally {
      setExcluindo(false)
    }
  }

  function conteudo() {
    if (carregando) {
      return (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
          <CircularProgress aria-label="Carregando regras" />
        </Box>
      )
    }
    if (erroAoCarregar) {
      return (
        <Alert
          severity="error"
          action={
            <Button color="inherit" onClick={tentarNovamente}>
              Tentar novamente
            </Button>
          }
        >
          {erroAoCarregar}
        </Alert>
      )
    }
    if (regras.length === 0) {
      return (
        <Paper variant="outlined" sx={{ p: 4, textAlign: 'center' }}>
          <Typography color="text.secondary">
            Nenhuma regra cadastrada. Clique em "Nova regra" para criar a primeira.
          </Typography>
        </Paper>
      )
    }
    return regras.map((regra) => (
      <Paper key={regra.id} variant="outlined" sx={{ p: 2, opacity: regra.ativa ? 1 : 0.6 }}>
        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          spacing={1}
          sx={{ alignItems: { sm: 'center' }, justifyContent: 'space-between' }}
        >
          <Stack direction="row" spacing={1} sx={{ alignItems: 'center', flexWrap: 'wrap' }}>
            <Typography variant="h6" component="h2">
              {regra.nome}
            </Typography>
            <Chip
              size="small"
              label={regra.tipo === 'seguidas' ? 'Faltas seguidas' : 'Faltas no período'}
              color={regra.tipo === 'seguidas' ? 'primary' : 'secondary'}
              variant="outlined"
            />
          </Stack>
          <Stack direction="row" spacing={1} sx={{ alignItems: 'center', flexShrink: 0 }}>
            <FormControlLabel
              control={
                <Switch
                  checked={regra.ativa}
                  disabled={alternando.has(regra.id)}
                  onChange={() => alternarAtiva(regra)}
                />
              }
              label={regra.ativa ? 'Ativa' : 'Desativada'}
              // Largura fixa para o switch não mudar de lugar quando o texto troca.
              slotProps={{ typography: { sx: { width: '6rem' } } }}
            />
            <Tooltip title="Editar">
              <IconButton aria-label={`Editar ${regra.nome}`} onClick={() => setEdicao(regra)}>
                <EditOutlinedIcon />
              </IconButton>
            </Tooltip>
            <Tooltip title="Excluir">
              <IconButton aria-label={`Excluir ${regra.nome}`} onClick={() => setParaExcluir(regra)}>
                <DeleteOutlinedIcon />
              </IconButton>
            </Tooltip>
          </Stack>
        </Stack>
        <Typography color="text.secondary" sx={{ mt: 1, pt: 1, borderTop: 1, borderColor: 'divider' }}>
          Irregular quando: {descreverRegra(regra)}
        </Typography>
      </Paper>
    ))
  }

  return (
    <Stack spacing={3}>
      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        spacing={2}
        sx={{ justifyContent: 'space-between', alignItems: { sm: 'flex-end' } }}
      >
        <Box>
          <Typography variant="h4" component="h1" gutterBottom sx={{ fontWeight: 700 }}>
            Regras de irregularidade
          </Typography>
          <Typography color="text.secondary">
            Defina quando a frequência de um aluno deve ser considerada irregular.
          </Typography>
        </Box>
        <Button
          variant="contained"
          size="large"
          startIcon={<AddIcon />}
          onClick={() => setEdicao('nova')}
          disabled={carregando || !!erroAoCarregar}
          sx={{ flexShrink: 0 }}
        >
          Nova regra
        </Button>
      </Stack>

      {conteudo()}

      {edicao && (
        <FormularioRegra
          inicial={
            edicao === 'nova' ? undefined : { ...edicao, periodoEmDias: edicao.periodoEmDias ?? PERIODO_PADRAO }
          }
          onSalvar={salvar}
          onCancelar={() => setEdicao(null)}
        />
      )}

      <Dialog open={!!paraExcluir} onClose={excluindo ? undefined : () => setParaExcluir(null)}>
        <DialogTitle>Excluir regra?</DialogTitle>
        <DialogContent>
          <Typography>
            A regra <strong>{paraExcluir?.nome}</strong> será removida. Essa ação não pode ser desfeita.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setParaExcluir(null)} disabled={excluindo}>
            Cancelar
          </Button>
          <Button color="error" variant="contained" onClick={excluir} loading={excluindo}>
            Excluir
          </Button>
        </DialogActions>
      </Dialog>

      <Snackbar open={!!aviso} autoHideDuration={4000} onClose={() => setAviso(null)} message={aviso} />
    </Stack>
  )
}

export default RegrasIrregularidade
