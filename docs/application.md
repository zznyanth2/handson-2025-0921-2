# ToDo アプリケーション - アプリケーション構成 & データモデル

作成日: 2025-09-21

このドキュメントは、`docs/requirements.md` の要件（ベーシック認証、Thymeleaf、開発: H2 / 本番: PostgreSQL、ドラッグ＆ドロップ必須、論理削除）を受けて、実装に必要なアーキテクチャ、データモデル、API、画面構成、実装ノートをまとめたものです。

1. 技術スタック
--
- Java 17+
- Spring Boot (starter-web, starter-thymeleaf, starter-data-jpa, starter-security, validation)
- DB: H2 (dev), PostgreSQL (prod)
- ビルド: Maven
- Frontend: Thymeleaf テンプレート + 最小限の JavaScript（ドラッグ＆ドロップ用に HTML5 DnD または小さなライブラリ）

2. プロジェクト構成（推奨パッケージ）
--
- com.example.todo
  - controller
  - service
  - repository
  - model (entity)
  - dto
  - config
  - security
  - exception

3. ドメインモデル
--
- Todo
  - id: UUID (PK)
  - title: String (not null, max 255)
  - description: String (nullable, max 2000)
  - due_date: LocalDate (nullable)
  - status: Enum (TODO, DOING, COMPLETED)
  - created_at: Instant
  - updated_at: Instant
  - deleted_at: Instant (nullable) // 論理削除用

DDL (概略, PostgreSQL 用)

CREATE TABLE todos (
  id UUID PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  due_date DATE,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE
);

4. REST API（Controller 層の概要）
--
- GET /api/todos
  - クエリ: status, q, dueBefore, page, size, sort
- POST /api/todos
- GET /api/todos/{id}
- PUT /api/todos/{id}
- PATCH /api/todos/{id}/status
- DELETE /api/todos/{id} (論理削除: deleted_at を現在時刻で設定)

レスポンスは JSON。フロントは Thymeleaf 経由でレンダリングするが、ドラッグ＆ドロップなど UI 操作は JavaScript から上記 API を呼ぶ。

5. セキュリティ
--
- ベーシック認証を使用する。Spring Security の HTTP Basic を組み込み、ユーザー情報はプロパティ（開発）または外部認証ストア（本番）で管理する。
- CSRF: Thymeleaf と Spring Security を組み合わせて CSRF トークンをフォームに埋め込む。API 呼び出し (AJAX) は CSRF ヘッダを含める。

6. フロントエンド設計（Thymeleaf ベース）
--
- ルート: GET / - メイン画面（ステータス別のカラム表示: TODO / DOING / COMPLETED）
- 各カラムはサーバーで初回レンダリングされるが、ドラッグ＆ドロップ操作はクライアントで処理して PATCH /api/todos/{id}/status を呼ぶ。
- 新規作成はモーダルまたは画面上部のフォームから POST /api/todos を呼ぶ。
- 編集はモーダルで PUT を呼ぶ。

7. ドラッグ＆ドロップ実装案
--
- HTML5 の Drag and Drop API を使うか、軽量ライブラリ（Sortable.js 等）を利用する。
- DnD 完了時に PATCH /api/todos/{id}/status を呼び、成功時に UI を更新する。失敗時は元に戻す。

8. 永続化/マイグレーション
--
- 開発: application-dev.yml で H2 を自動生成（spring.jpa.hibernate.ddl-auto=update）
- 本番: Flyway または Liquibase を使用してマイグレーションを管理（PostgreSQL）

9. DTO とバリデーション
--
- TodoCreateDto { title, description, dueDate, status }
- TodoUpdateDto { title, description, dueDate, status }
- バリデーション注釈: @NotBlank, @Size, @PastOrPresent?（期日要件に応じて）

10. Repository 層
--
- interface TodoRepository extends JpaRepository<Todo, UUID> {
  Page<Todo> findByStatusAndDeletedAtIsNull(Status status, Pageable p);
  Page<Todo> findByDeletedAtIsNull(Pageable p);
  // 検索用メソッド等
}

11. サービス層（注意点）
--
- 論理削除を考慮して全ての検索は deleted_at IS NULL を条件に含める。
- ステータス更新はトランザクション内で行い、必要ならイベントを発行して監査ログを残す。

12. 例外ハンドリング
--
- @ControllerAdvice で REST と MVC の例外をハンドリングし、一貫したエラー表示を行う。

13. ログと監査
--
- 主要操作（作成・更新・削除・ステータス変更）は INFO レベルでログ出力。詳細は DEBUG。

14. 開発時チェックリスト
--
1. Maven ビルドが通ることを確認する（mvn -DskipTests package）
2. H2 で主要 API の動作確認
3. Thymeleaf テンプレートで DnD が正常に動作すること
4. Basic Auth と CSRF トークンの連携確認
5. Flyway マイグレーション作成（prod 用）

15. 未決定 / 今後のタスク
--
- ユーザー管理: 現状は固定ユーザーを想定。複数ユーザー・ユーザー別 ToDo を要件に含める場合は設計を拡張する。
- API 仕様を OpenAPI 化するかどうか。

次のステップ
--
1. この構成で実装を開始してよいか確認ください。
2. 承認後、サンプルコントローラ、エンティティ、リポジトリ、テンプレートの雛形を作成します。

---

作成者: 要件定義自動生成ツール（ユーザー指定オプション反映）
