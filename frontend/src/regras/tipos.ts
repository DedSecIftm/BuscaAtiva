export type TipoRegra = 'seguidas' | 'noPeriodo'

export interface Regra {
  id: string
  nome: string
  tipo: TipoRegra
  quantidadeFaltas: number
  // Usado apenas quando o tipo é 'noPeriodo'.
  periodoEmDias: number
  ativa: boolean
}

export type DadosRegra = Omit<Regra, 'id' | 'ativa'>

function plural(quantidade: number, singular: string, varias: string): string {
  return `${quantidade} ${quantidade === 1 ? singular : varias}`
}

export function descreverRegra(regra: DadosRegra): string {
  const faltas = plural(regra.quantidadeFaltas, 'falta', 'faltas')
  if (regra.tipo === 'seguidas') {
    return `O aluno tiver ${faltas} ${regra.quantidadeFaltas === 1 ? 'seguida' : 'seguidas'}.`
  }
  return `O aluno tiver ${faltas}, seguidas ou não, dentro de ${plural(regra.periodoEmDias, 'dia', 'dias')}.`
}

// Retorna a mensagem de erro de cada campo inválido.
export function validarRegra(regra: DadosRegra): Partial<Record<keyof DadosRegra, string>> {
  const erros: Partial<Record<keyof DadosRegra, string>> = {}
  if (!regra.nome.trim()) erros.nome = 'Dê um nome para a regra.'
  if (!Number.isInteger(regra.quantidadeFaltas) || regra.quantidadeFaltas < 1) {
    erros.quantidadeFaltas = 'Informe um número de faltas maior que zero.'
  }
  if (regra.tipo === 'noPeriodo') {
    if (!Number.isInteger(regra.periodoEmDias) || regra.periodoEmDias < 1) {
      erros.periodoEmDias = 'Informe um período maior que zero.'
    } else if (regra.periodoEmDias < regra.quantidadeFaltas) {
      erros.periodoEmDias = 'O período precisa ter pelo menos a mesma quantidade de dias que as faltas.'
    }
  }
  return erros
}
