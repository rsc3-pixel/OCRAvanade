import { useRef, useState, type DragEvent } from 'react'
import { Icone } from './Icone'

export const TAMANHO_MAXIMO = 10 * 1024 * 1024
const ACEITOS = ['application/pdf', 'image/png', 'image/jpeg']

function formatarTamanho(bytes: number) {
  return bytes < 1024 * 1024 ? `${Math.round(bytes / 1024)} KB` : `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

/** Valida no navegador antes de enviar: poupa uma ida ao servidor para erros óbvios. */
function validar(arquivo: File): string | null {
  if (!ACEITOS.includes(arquivo.type)) return 'Formato não suportado. Envie PDF, PNG ou JPG.'
  if (arquivo.size > TAMANHO_MAXIMO) return 'Arquivo maior que 10 MB.'
  return null
}

interface Props {
  arquivo: File | null
  desabilitado: boolean
  aoEscolher: (arquivo: File | null) => void
  aoErro: (mensagem: string) => void
}

export function AreaUpload({ arquivo, desabilitado, aoEscolher, aoErro }: Props) {
  const input = useRef<HTMLInputElement>(null)
  const [arrastando, setArrastando] = useState(false)

  function receber(lista: FileList | null) {
    const escolhido = lista?.[0]
    if (!escolhido) return
    const erro = validar(escolhido)
    if (erro) {
      aoErro(erro)
      return
    }
    aoEscolher(escolhido)
  }

  function soltar(e: DragEvent) {
    e.preventDefault()
    setArrastando(false)
    if (!desabilitado) receber(e.dataTransfer.files)
  }

  return (
    <>
      <div
        className={`soltar${arrastando ? ' soltar--ativo' : ''}`}
        onDragOver={(e) => {
          e.preventDefault()
          setArrastando(true)
        }}
        onDragLeave={() => setArrastando(false)}
        onDrop={soltar}
      >
        <span className="soltar__icone">
          <Icone nome="upload" tamanho={44} />
        </span>
        <p className="soltar__titulo">Arraste o arquivo aqui ou selecione no aparelho</p>
        <p className="soltar__formatos">PDF, PNG ou JPG, até 10 MB</p>
        <button type="button" className="botao botao--primario" disabled={desabilitado} onClick={() => input.current?.click()}>
          Selecionar arquivo
        </button>
        <input
          ref={input}
          type="file"
          accept=".pdf,.png,.jpg,.jpeg"
          hidden
          onChange={(e) => {
            receber(e.target.files)
            e.target.value = ''
          }}
        />
      </div>

      {arquivo && (
        <div className="arquivo">
          <span className="arquivo__icone">
            <Icone nome="arquivo" tamanho={26} />
          </span>
          <span className="arquivo__info">
            <strong>{arquivo.name}</strong>
            <small>{formatarTamanho(arquivo.size)}</small>
          </span>
          <span className="arquivo__ok">
            <Icone nome="check" tamanho={16} />
            Pronto para enviar
          </span>
          <button
            type="button"
            className="botao-icone"
            aria-label="Remover arquivo"
            disabled={desabilitado}
            onClick={() => aoEscolher(null)}
          >
            <Icone nome="fechar" tamanho={18} />
          </button>
        </div>
      )}
    </>
  )
}
