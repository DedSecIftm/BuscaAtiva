import Alert from '@mui/material/Alert'
import AlertTitle from '@mui/material/AlertTitle'
import Box from '@mui/material/Box'
import type { ResultadoDaImportacao as Resultado } from '../importacao/api.ts'

const MAXIMO_DE_ERROS = 10

export type ResultadoDoArquivo = { nome: string } & ({ resultado: Resultado } | { falha: string })

function resumo({ turmasCriadas, alunosCriados, alunosAtualizados, linhasComErro }: Resultado): string {
  const partes = [
    `${alunosCriados} ${alunosCriados === 1 ? 'aluno cadastrado' : 'alunos cadastrados'}`,
    `${alunosAtualizados} ${alunosAtualizados === 1 ? 'atualizado' : 'atualizados'}`,
  ]
  if (turmasCriadas > 0) {
    partes.push(`${turmasCriadas} ${turmasCriadas === 1 ? 'turma cadastrada' : 'turmas cadastradas'}`)
  }
  if (linhasComErro > 0) {
    partes.push(`${linhasComErro} ${linhasComErro === 1 ? 'linha com erro' : 'linhas com erro'}`)
  }
  return partes.join(' · ')
}

function ResultadoDaImportacao({ item, onFechar }: { item: ResultadoDoArquivo; onFechar: () => void }) {
  if ('falha' in item) {
    return (
      <Alert severity="error" onClose={onFechar}>
        <AlertTitle sx={{ wordBreak: 'break-all' }}>{item.nome}</AlertTitle>
        {item.falha}
      </Alert>
    )
  }

  const { erros } = item.resultado
  const ocultos = erros.length - MAXIMO_DE_ERROS
  return (
    <Alert severity={erros.length > 0 ? 'warning' : 'success'} onClose={onFechar}>
      <AlertTitle sx={{ wordBreak: 'break-all' }}>{item.nome}</AlertTitle>
      {resumo(item.resultado)}
      {erros.length > 0 && (
        <Box component="ul" sx={{ m: 0, mt: 1, pl: 2.5 }}>
          {erros.slice(0, MAXIMO_DE_ERROS).map((mensagem) => (
            <li key={mensagem}>{mensagem}</li>
          ))}
          {ocultos > 0 && <li>E mais {ocultos}.</li>}
        </Box>
      )}
    </Alert>
  )
}

export default ResultadoDaImportacao
