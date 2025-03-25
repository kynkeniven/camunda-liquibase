# Camunda 7 com Liquibase

Este tutorial mostra como configurar o Liquibase no Camunda 7 e como realizar migrações futuras.

## 🎥 Tutorial gravado no Pit Tech da NTConsult

[Assista ao vídeo](https://ntconsultcorpusa.sharepoint.com/sites/intranet/_layouts/15/stream.aspx?id=%2Fsites%2Fintranet%2FVdeos%2FPitch%20Tech%20NT%20%2D%20Migrando%20o%20Banco%20de%20Dados%20do%20Camunda%20com%20Liquibase%2D20240710%5F130238%2DGravação%20de%20Reunião%2Emp4&referrer=StreamWebApp%2EWeb&referrerScenario=AddressBarCopied%2Eview%2E16a0afd5%2Dcc69%2D4d72%2Da689%2D5b3b31efc061)

## 📚 Documentação oficial
[Camunda Database Schema](https://docs.camunda.org/manual/latest/installation/database-schema/)

## 🔧 Instalar o Liquibase Client
[Download do Liquibase](https://www.liquibase.com/download)

## 📂 Repositório oficial com os scripts do Liquibase
[Camunda SQL Scripts](https://artifacts.camunda.com/ui/native/camunda-bpm/org/camunda/bpm/distro/camunda-sql-scripts/)

### 📌 Como configurar os scripts no projeto
1. Escolha a versão do Camunda que está utilizando.
2. Baixe os scripts no formato `.zip`.
3. No projeto, dentro da pasta `resources`, cole a pasta `liquibase` que se encontra dentro do `.zip`.
4. Mantenha apenas os scripts referentes ao seu banco de dados, excluindo os demais.
5. No arquivo `camunda-changelog.xml`, deixe somente a tag `<property>` referente ao seu banco de dados, excluindo todas as outras.

## ⚙️ Configuração no `application.yaml`

```yaml
spring:
    liquibase:
        enabled: true
        change-log: classpath:liquibase/camunda-changelog.xml

camunda:
    bpm:
        database:
            schema-update: false
```

- Habilite o Liquibase e aponte para o seu `camunda-changelog.xml`.

- Desabilite o `schema-update` do Camunda para evitar que ele gerencie o banco de dados automaticamente.

## ▶️ Comandos do Liquibase

### 🚀 Criar o banco de dados do Camunda 

Se o BD do camunda ainda não existe, rode o comando abaixo para fazer a instalação. 
Somente habilitando o liquibase no yaml, não será suficiente a primeira vez para gerar o banco de dados.
Pois quando a aplicação rodar, o Camunda tentará se conectar ao banco antes do liquibase gerar ele.

Rodar a partir da pasta `resources`:
```sh
liquibase update --changelog-file=liquibase/camunda-changelog.xml --url=jdbc:postgresql://localhost:5432/camunda --username=admin --password=admin
```

### 🔍 Visualizar SQL antes da execução

Rodar a partir da pasta `resources`:
```sh
liquibase update-sql --changelog-file=liquibase/camunda-changelog.xml --url=jdbc:postgresql://localhost:5432/camunda --username=admin --password=admin
```

### 🏗️ Sincronizar Liquibase com o estado atual do banco

Rodar a partir da pasta `resources`: 
```sh
liquibase changelog-sync-to-tag --tag=7.20.0 --changelog-file=liquibase/camunda-changelog.xml --url=jdbc:postgresql://localhost:5432/camunda --username=admin --password=admin
```
Trocar o --tag-7.20.0 pela sua versao do Camunda

## 🔄 Atualização do banco de dados do Camunda usando Liquibase

Quando uma nova versão do Camunda for lançada:

1. Baixe os novos scripts do repositório oficial.
2. Copie para o projeto apenas os scripts da pasta `upgrade` referentes ao seu banco que ainda não existam no seu projeto.
    - **Exemplo**: Se está no Camunda `7.21` e deseja migrar para `7.22`, copie o script de update `7.21_to_7.22`.
3. Substitua o `camunda-changelog.xml` pelo da nova versão e remova as tags `<property>` de bancos não utilizados.
4. Reinicie o Camunda para que o banco seja atualizado via Liquibase **ou** rode manualmente o comando abaixo (mesmo comando usado na criação do BD):

```sh
liquibase update --changelog-file=liquibase/camunda-changelog.xml --url=jdbc:postgresql://localhost:5432/camunda --username=admin --password=admin
```

## ⚠️ Scripts Liquibase Dentro do Projeto

Existe uma maneira mais eficiente de trabalhar com liquibase no Camunda, onde os scripts do liquibase
já vem dentro do external jar camunda-engine.
Basta apontar para o camunda-changelog.xml localizado no jar e criar o plugin maven do 
liquibase para que os scripts sejam rodados.

- YAML apontando para o changelog no external jar
```yaml
spring:
    liquibase:
        enabled: true
        change-log: classpath:org/camunda/bpm/engine/db/liquibase/camunda-changelog.xml

```

- Plugin maven do liquibase que vai pegar o changelog e os scripts do jar camunda-engine.
```pom
 <plugin>
    <groupId>org.liquibase</groupId>
    <artifactId>liquibase-maven-plugin</artifactId>
    <version>${liquibase.version}</version>
    <executions>
        <execution>
            <goals>
                <goal>update</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <changeLogFile>org/camunda/bpm/engine/db/liquibase/camunda-changelog.xml</changeLogFile>
        <url>jdbc:postgresql://localhost:5432/camunda</url>
        <username>admin</username>
        <password>admin</password>
    </configuration>
</plugin>
```

- Comando para criar/atualizar banco de dados
Lembrando que como o Camunda não está gerando o BD, é necesario rodar o comando antes de rodar o Camunda.
```sh
mvn clean liquibase:update
```
- Ao atualizar a versão do Camunda no POM, os novos scripts do liquibase ja serão incorporados.
- Basta rodar o comando acima ou reiniciar o Camunda, que a migração do BD via liquibase irá ocorrer.