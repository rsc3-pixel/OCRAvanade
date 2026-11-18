import type { Dados, Envio } from '../api'
import { Icone } from './Icone'

const ROTULOS: Record<keyof Dados, string> = {
  nome: 'Nome',
  cpf: 'CPF',
  endereco: 'Endereço',
  cidade: 'Cidade',
  estado: 'UF',
  dataEmissao: 'Emissão',
}

function formatar(campo: keyof Dados, valor: string | null) {
  if (!valor) return 'Não encontrado'
  if (campo === 'dataEmissao') return new Date(`${valor}T12:00:00`).toLocaleDateString('pt-BR')
  return valor
}

export function Resultado({ envio, aoNovoEnvio }: { envio: Envio; aoNovoEnvio: () => void }) {
  const aprovado = envio.status === 'APROVADO'
  return (
    <div className="resultado">
      <div className={`veredito veredito--${aprovado ? 'aprovado' : 'reprovado'}`}>
        <span className="veredito__icone">
          <Icone nome={aprovado ? 'check' : 'fechar'} tamanho={28} />
        </span>
        <div>
          <strong>{aprovado ? 'Aprovado' : 'Reprovado'}</strong>
          <p>{aprovado ? 'Seu comprovante passou em todas as regras.' : 'Corrija os itens marcados e envie de novo.'}</p>
        </div>
      </div>

      <h3 className="secao-titulo">Regras aplicadas</h3>
      <ul className="regras">
        {envio.regras.map((r) => (
          <li key={r.regra} className={`regra regra--${r.aprovada ? 'ok' : 'falha'}`}>
            <Icone nome={r.aprovada ? 'check' : 'fechar'} tamanho={18} />
            <span>
              <strong>{r.regra}</strong>
              <small>{r.detalhe}</small>
            </span>
          </li>
        ))}
      </ul>

      <h3 className="secao-titulo">Dados extraídos</h3>
      <dl className="dados">
        {(Object.keys(ROTULOS) as (keyof Dados)[]).map((campo) => (
          <div key={campo} className="dados__item">
            <dt>{ROTULOS[campo]}</dt>
            <dd>{formatar(campo, envio.dados[campo])}</dd>
          </div>
        ))}
      </dl>

      <details className="texto-ocr">
        <summary>Ver texto lido pelo OCR (extrator: {envio.extrator})</summary>
        <pre>{envio.textoExtraido || '(nenhum texto lido)'}</pre>
      </details>

      <div className="acoes">
        <button type="button" className="botao botao--primario" onClick={aoNovoEnvio}>
          Enviar outro documento
        </button>
      </div>
    </div>
  )
}
