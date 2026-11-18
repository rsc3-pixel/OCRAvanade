# Encontro 2: do problema à solução

Mentoria 2026.2 · Residência Tecnológica · Recife

Este documento responde às perguntas do Encontro 2 para o projeto **OCRAvanade**: um MVP que recebe um
comprovante de residência, lê o texto da imagem e decide se o documento é aceito ou recusado.

## 1. Entender

**Qual é o problema?**
Empresas que fazem cadastro de clientes (bancos, operadoras, programas sociais, RH) pedem comprovante de
residência. Hoje alguém abre o arquivo, lê nome, CPF, endereço e data, digita num sistema e decide se o
documento vale. É lento, repetitivo e sujeito a erro de digitação.

**Quem sofre com ele hoje?**
- **Analista de cadastro:** passa o dia conferindo documento por documento.
- **Cliente:** espera dias pela aprovação e muitas vezes descobre tarde que mandou um documento vencido.
- **Empresa:** paga caro por um processo manual e tem risco de aceitar documento inválido.

**Quais são as principais dores?**
1. Tempo de análise alto (minutos por documento, dias de fila).
2. Erro humano na transcrição dos dados.
3. Critério de aceite inconsistente entre analistas.
4. Retrabalho: o cliente só descobre o problema depois de esperar.

## 2. Definir

**Qual documento vamos processar?**
Comprovante de residência (conta de água, luz, telefone ou fatura de cartão) em **PDF, PNG ou JPG, até 10 MB**.
Começamos com um único tipo de documento de propósito: é o menor escopo que prova o fluxo inteiro.

**Quais informações precisamos extrair?**

| Campo | Exemplo | Por que importa |
|---|---|---|
| Nome do titular | João da Silva | Conferir se o comprovante é da pessoa |
| CPF | 123.456.789-09 | Identificador único do titular |
| Endereço | Rua das Flores, 150 | É o dado que o cadastro precisa |
| Cidade / UF | Guanambi / BA | Completa o endereço |
| Data de emissão | 10/11/2026 | Define se o comprovante é recente |

**O que torna esse documento válido ou inválido?**

| Regra | Aprova quando | Reprova quando |
|---|---|---|
| Documento legível | O OCR extrai texto suficiente | Imagem borrada, cortada ou escura |
| Nome encontrado | Existe um nome de titular | Nome ausente |
| CPF encontrado | Existe um CPF com 11 dígitos | CPF ausente ou incompleto |
| Endereço encontrado | Existe endereço | Endereço ausente |
| Emissão recente | Emitido nos últimos 90 dias | Mais antigo que 90 dias, sem data, ou data no futuro |

Todas as regras precisam passar para o documento ser **Aprovado**. Qualquer falha gera **Reprovado**, e a tela
mostra exatamente qual regra falhou, para o cliente corrigir na hora.

## 3. Desenhar

**Como será o fluxo da solução?**

```
Documento -> Upload -> OpenCV -> Tesseract (OCR) -> Extração (Regex ou IA) -> Validação -> Resultado + banco
```

| Etapa | Tecnologia | Papel |
|---|---|---|
| Upload | React + Spring Boot | Recebe o arquivo e o tipo de documento |
| Preparar | OpenCV (+ PDFBox para PDF) | Escala de cinza, remove ruído, preto e branco, corrige rotação |
| Ler | Tesseract (português) | Transforma a imagem em texto. Não entende o significado |
| Interpretar | Regex **ou** LLM (Claude) | Transforma o texto em campos estruturados (JSON) |
| Validar | Java (regras de negócio) | Aplica as regras da seção 2 e decide aprovado/reprovado |
| Persistir | PostgreSQL | Guarda o envio, os dados e o resultado de cada regra |

**Quais tecnologias podem ajudar?**
Java 21 + Spring Boot, OpenCV, Tesseract (via Tess4J), PDFBox, PostgreSQL, React + Vite, Docker.
A IA é **opcional**: o sistema roda com Regex sem custo e sem chave, e liga o LLM por variável de ambiente
quando os layouts ficarem variados demais para Regex.

## 4. Criar o MVP

**Qual é a menor solução que conseguimos construir e demonstrar?**
Uma tela de upload que envia um comprovante, mostra o progresso (Upload, Processamento, Validação, Resultado),
exibe os dados extraídos e o veredito com o motivo de cada regra. Tudo sobe com um único `docker compose up`.

**Por onde vamos começar?**
1. Regras de validação e extração por Regex, com testes (o coração do negócio, sem dependência externa).
2. Pipeline de imagem (OpenCV + Tesseract).
3. API REST e banco.
4. Tela de upload e resultado.
5. Extrator com IA como alternativa plugável.

## Resumo para a apresentação

| Pergunta | Resposta |
|---|---|
| **Usuário** | Cliente que envia o comprovante; analista que hoje confere à mão |
| **Documento** | Comprovante de residência (PDF/PNG/JPG, até 10 MB) |
| **Dados a extrair** | Nome, CPF, endereço, cidade/UF, data de emissão |
| **Regras de validação** | Legível, nome, CPF, endereço, emissão em até 90 dias |
| **Fluxo da solução** | Upload, OpenCV, Tesseract, Regex/IA, validação, resultado salvo no Postgres |
