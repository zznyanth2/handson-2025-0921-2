# ToDo アプリケーション 要件定義書

作成日: 2025-09-21

概要
--
本ドキュメントは、Spring Boot を用いてブラウザから利用できる ToDo（タスク管理）アプリケーションの要件定義をまとめたものです。本アプリはシンプルな CRUD 機能を備え、タスクの状態（Status）による区分表示を行います。

対象ユーザー
--
- 個人のタスク管理を行う一般ユーザー
- 小規模チームで簡易なタスク共有を行うユーザー

非機能要件
--
- 対応ブラウザ: 最新の Chrome、Firefox、Edge（モダンブラウザ）
- レスポンシブ: デスクトップ優先。モバイルは簡易対応
- セキュリティ: CSRF 対策、入力検証を実施
- ロギング: SLF4J を用いて主要アクションをログ出力

機能要件
--
1. ToDo アイテムの CRUD
   - 作成(Create): タスクを新規作成できる。必須入力はタイトル。
   - 参照(Read): タスク一覧およびタスク詳細を閲覧できる。
   - 更新(Update): タイトル、説明、期日、Status を編集できる。
   - 削除(Delete): タスクを削除できる（論理削除の検討可）。

2. Status プロパティ
   - ToDo アイテムは Status を持つ。Status は列挙型で次の 3 値を持つ:
     - TODO
     - DOING
     - COMPLETED

3. ステータス別表示画面
   - メイン画面で Status ごとに区切られた列（カンバン風）またはタブで表示する。
   - 各区画にその Status のタスク一覧を表示する。
   - タスクはドラッグアンドドロップで別の Status に変更できる（実装はオプション、API 必要）。

4. 検索・フィルタ
   - タイトルと説明でキーワード検索ができる。
   - 期日や Status でフィルタ可能。

5. 入力バリデーション
   - タイトル: 必須、最大 255 文字
   - 説明: 任意、最大 2000 文字
   - 期日: 日付フォーマット (ISO 8601)

6. API と画面設計（概要）
   - REST API を提供（JSON）
     - GET /api/todos - タスク一覧（クエリ: status, q=keyword, sort, page, size）
     - POST /api/todos - タスク作成
     - GET /api/todos/{id} - タスク詳細
     - PUT /api/todos/{id} - タスク更新
     - DELETE /api/todos/{id} - タスク削除
     - PATCH /api/todos/{id}/status - ステータス更新（簡易エンドポイント）
   - フロントエンドは Thymeleaf またはシングルページアプリ（React/Vue）で実装可能。初期は Thymeleaf を想定。

7. 永続化
   - RDBMS（H2 for dev, PostgreSQL for production を想定）
   - エンティティ例: Todo(id: UUID, title: String, description: String, dueDate: LocalDate, status: Enum, createdAt, updatedAt, deletedAt?)

8. エラーハンドリング
   - REST は一貫したエラー JSON 構造を返す (timestamp, status, error, message, path)
   - バリデーションエラーは 400、存在しないリソースは 404

受け入れ基準（Acceptance Criteria）
--
- 画面からタスクを作成すると、一覧に表示される
- ステータスを変更すると、該当ステータスの区画に移動する
- API の CRUD 操作が正しく動作する（ステータス変更含む）
- バリデーションエラーが適切に通知される

設計補足（開発向け）
--
- プロジェクト構成: 標準的な Spring Boot (starter-web, starter-data-jpa, validation)
- DI: コンストラクタ注入
- DTO 層を設け、Controller は DTO を受け渡す
- Repository は Spring Data JPA を使用
- サービス層でビジネスロジックを保持

API サンプル
--
1) タスク作成
   POST /api/todos
   Request JSON:
   {
     "title": "買い物",
     "description": "牛乳とパン",
     "dueDate": "2025-09-30",
     "status": "TODO"
   }

   Response: 201 Created, Location ヘッダに /api/todos/{id}

2) タスク一覧取得（ステータスフィルタ）
   GET /api/todos?status=DOING
   Response: 200 OK
   [ { "id": "...", "title": "...", "status": "DOING", ... }, ... ]

受け入れテスト（簡易）
--
- Happy path: タスクの作成 → 取得 → 更新(status を DOING) → 削除 が成功する
- バリデーション: 空タイトルで 400 が返る

不足確認事項 / 要選択のオプション
--
以下の点は要件によって実装方針が変わります。選択を教えてください。
1. 認証・認可: 必要ですか？（例: 無し / ベーシック認証 / OAuth2 / 独自ログイン）
2. フロントエンドの種類: サーバーサイドレンダリング（Thymeleaf）か、SPA（React/Vue）か？
3. 永続化DB: 開発は組み込み H2、実運用は PostgreSQL でよいですか？他に希望があれば。
4. ドラッグアンドドロップでのステータス変更は必須ですか？
5. ソフト削除（論理削除）を使いますか？それとも物理削除でよいですか？

次のステップ
--
1. 上記の不足確認事項について回答をください（優先度順）。
2. 回答に基づき要件書を最終化し、必要なら API 仕様（OpenAPI）や DB スキーマ、画面遷移図を追加します。

---

作成者: 要件定義自動生成ツール（初稿）

## 詳細な受け入れ基準とテスト案

### 受け入れシナリオ（UI: 手動 / E2E）
- シナリオ A — 新規作成と一覧表示
   1. ログイン（Basic Auth）する
   2. メイン画面で「新規作成」フォームにタイトルを入力して作成する
   3. 作成後、作成したタスクが該当の Status 列（既定は TODO）に表示される

- シナリオ B — 編集とステータス移動（ドラッグ＆ドロップ）
   1. メイン画面で TODO 列にあるタスクを DOING 列へドラッグ＆ドロップする
   2. クライアントは PATCH /api/todos/{id}/status を呼び、サーバは 200 を返す
   3. UI は DOING 列にタスクを移動して表示する

- シナリオ C — 削除（論理削除）
   1. タスクの削除操作を行う
   2. サーバは対象レコードの deleted_at を設定して 204/200 を返す
   3. 一覧では見えなくなる（deleted_at があるものは除外される）

### API レベルのテストケース（自動化可能）

1) 作成ハッピーパス
    - Request: POST /api/todos
       {
          "title": "買い物",
          "description": "牛乳",
          "dueDate": "2025-09-30",
          "status": "TODO"
       }
    - Expect: 201 Created
    - Assert: Location ヘッダに /api/todos/{id}、GET で内容が一致

2) バリデーションエラー
    - Request: POST /api/todos with { "title": "" }
    - Expect: 400 Bad Request
    - Assert: エラーメッセージにタイトル必須を含む

3) ステータス更新
    - Request: PATCH /api/todos/{id}/status { "status": "DOING" }
    - Expect: 200 OK
    - Assert: GET /api/todos/{id} の status が DOING

4) 論理削除
    - Request: DELETE /api/todos/{id}
    - Expect: 204 No Content (or 200)
    - Assert: GET /api/todos/{id} が 404 を返す（もしくは deleted_at フィールドがセットされるが一覧から除外される）

5) 認証保護の確認
    - 未認証で API を呼ぶ
    - Expect: 401 Unauthorized

### E2E テストフロー（自動化テスト）
- 前提: テスト用ユーザーに対する Basic Auth 資格情報を用意
1. POST でタスクを 1 件作成
2. GET で一覧確認（作成タスクが TODO 列に存在）
3. PATCH で status を DOING に変更
4. GET で status 変更を確認
5. DELETE で削除
6. GET で存在しないことを確認

### UI テスト（ドラッグ＆ドロップのテスト）
- Playwright / Selenium 等で次を確認:
   - タスクをドラッグして別カラムへ移動すると、サーバへ PATCH が送られる
   - サーバが成功応答した場合 UI が更新される
   - サーバエラー（500 など）は UI 上で元の位置に戻す

### エッジケース
- 大量のタスク（ページネーションの確認）
- 同時更新（楽観ロック or 更新順序の説明）
- 不正なステータス値が来た場合は 400 を返す

### 簡易テストデータ
- title が空、最大長、特殊文字を含むケース
- dueDate が過去日・将来日・不正フォーマット

### テスト実行メモ（手順）
1. 開発プロファイルでアプリを起動（H2）
2. テストユーザーで Basic Auth を付与して API を叩く
3. Playwright / JUnit + SpringBootTest で API と UI の E2E を自動化

---

この追記により、受け入れ基準と検証方法が具体化されました。次は要件に基づくコード雛形（Entity/Repository/Controller/Service/Thymeleaf テンプレート）を作成できます。
