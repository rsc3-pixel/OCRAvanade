# Exercícios: evoluindo o MVP

Cada item tem **onde mexer**, **o que fazer** e **como saber que deu certo**. Comece pelo teste sempre que possível:
escreva o teste que falha, depois o código que faz ele passar.

## 1. Validar o dígito verificador do CPF (concluído)

**Onde:** `backend/src/main/java/br/com/ocravanade/validacao/ValidadorDocumento.java`, método `cpf` (linha 48).

Feito em `validacao/Cpf.java`, com testes em `CpfTest` e `ValidadorDocumentoTest`. Antes, a regra só conferia se existiam
11 dígitos e `111.111.111-11` passava.

**Dica:** os dois últimos dígitos são calculados a partir dos nove primeiros (pesos de 10 a 2 para o primeiro
dígito, de 11 a 2 para o segundo, resto da divisão por 11). CPFs com todos os dígitos iguais são inválidos.

**Pronto quando:** um teste novo em `ValidadorDocumentoTest` mostra que `12345678909` passa e `12345678900` e
`11111111111` reprovam.

## 2. Cidade e UF não aparecem no PDF (próximo)

**Onde:** `backend/src/main/java/br/com/ocravanade/extracao/ExtratorRegex.java`, padrão `CIDADE_UF` (linha 22).

Envie `amostras/comprovante-aprovado.pdf` e abra "Ver texto lido pelo OCR" na tela. Compare a linha
`Recife - PE` com o que o Tesseract leu e ajuste o Regex.

**Pronto quando:** o teste com o texto real lido do PDF extrai `Recife` e `PE`.

## 3. Mostrar a imagem tratada pelo OpenCV

**Onde:** `PreProcessadorImagem.tratar` (já devolve a imagem), `EnvioService.processar` e a tela de resultado.

O slide mostra o antes e depois do OpenCV. Mostrar isso na tela ajuda o usuário a entender por que uma foto ruim
foi reprovada. Decida: guardar a imagem no banco ou devolver junto na resposta? Pense em tamanho e em LGPD.

## 4. Comparar Regex e IA

Ligue `OCR_EXTRATOR=CLAUDE` (veja o README), envie as mesmas amostras e compare os campos extraídos. Anote
quando cada um acerta mais e quanto custou. Isso vira um ótimo slide para a mentoria.

## 5. Migrações de banco com Flyway

**Onde:** `backend/src/main/resources/application.properties`, linha 7 (`ddl-auto=update`).

O `update` deixa o Hibernate mudar o banco sozinho, o que é perigoso em produção. Troque por Flyway com um script
`V1__criar_envios.sql` e `ddl-auto=validate`.

## Perguntas de entrevista para treinar

1. Por que o pré-processamento filtra o ruído antes de ampliar a imagem, e não depois?
2. O Tesseract "entende" que um número é um CPF? Se não, qual etapa entende?
3. Por que existe um `Clock` injetado no `ValidadorDocumento` em vez de chamar `LocalDate.now()` direto?
