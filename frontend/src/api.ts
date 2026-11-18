// Tipos espelham o EnvioResposta do backend (Java). Se mudar lá, muda aqui.
export type Status = 'APROVADO' | 'REPROVADO'

export interface Regra {
  regra: string
  aprovada: boolean
  detalhe: string
}

export interface Dados {
  nome: string | null
  cpf: string | null
  endereco: string | null
  cidade: string | null
  estado: string | null
  dataEmissao: string | null
}

export interface Envio {
  id: number
  nomeArquivo: string
  tipo: 'COMPROVANTE_RESIDENCIA'
  status: Status
  extrator: string
  textoExtraido: string
  dados: Dados
  regras: Regra[]
  criadoEm: string
}

async function lerErro(resposta: Response): Promise<never> {
  const corpo = await resposta.json().catch(() => null)
  throw new Error(corpo?.detail ?? `Erro ${resposta.status}`)
}

export async function enviarDocumento(arquivo: File, tipo: string): Promise<Envio> {
  const form = new FormData()
  form.append('arquivo', arquivo)
  form.append('tipo', tipo)
  const resposta = await fetch('/api/envios', { method: 'POST', body: form })
  return resposta.ok ? resposta.json() : lerErro(resposta)
}

export async function listarEnvios(): Promise<Envio[]> {
  const resposta = await fetch('/api/envios')
  return resposta.ok ? resposta.json() : lerErro(resposta)
}
