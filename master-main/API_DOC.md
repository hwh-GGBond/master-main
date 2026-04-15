# DocPlatform API 接口文档

## 1. 认证接口

### 1.1 注册接口

**请求方法**：POST
**请求路径**：`/api/auth/register`
**请求体**：
```json
{
  "username": "string",
  "password": "string"
}
```
**成功响应**：
```json
{
  "token": "string",
  "username": "string"
}
```
**失败响应**：
```json
{
  "error": "Username already exists"
}
```

### 1.2 登录接口

**请求方法**：POST
**请求路径**：`/api/auth/login`
**请求体**：
```json
{
  "username": "string",
  "password": "string"
}
```
**成功响应**：
```json
{
  "token": "string",
  "username": "string"
}
```
**失败响应**：
```json
{
  "error": "Bad credentials"
}
```

## 2. 文档管理接口

### 2.1 上传文档

**请求方法**：POST
**请求路径**：`/api/documents/upload`
**请求头**：
```
Authorization: Bearer {token}
```
**请求体**：
```
FormData: {
  "file": File
}
```
**成功响应**：
```json
{
  "id": 1,
  "title": "string",
  "originalName": "string",
  "filePath": "string",
  "mdContent": null,
  "fileSize": 1024,
  "fileType": "application/pdf",
  "converted": false,
  "userId": 1,
  "createdAt": "2026-04-13T17:00:00",
  "updatedAt": "2026-04-13T17:00:00"
}
```
**失败响应**：
```json
{
  "error": "Failed to upload document"
}
```

### 2.2 转换文档

**请求方法**：POST
**请求路径**：`/api/documents/{id}/convert`
**请求头**：
```
Authorization: Bearer {token}
```
**成功响应**：
```json
{
  "id": 1,
  "title": "string",
  "originalName": "string",
  "filePath": "string",
  "mdContent": "# Document\n\nContent...",
  "fileSize": 1024,
  "fileType": "application/pdf",
  "converted": true,
  "userId": 1,
  "createdAt": "2026-04-13T17:00:00",
  "updatedAt": "2026-04-13T17:00:00"
}
```
**失败响应**：
```json
{
  "error": "Unsupported file type for conversion"
}
```

### 2.3 获取文档列表

**请求方法**：GET
**请求路径**：`/api/documents`
**请求头**：
```
Authorization: Bearer {token}
```
**成功响应**：
```json
[
  {
    "id": 1,
    "title": "string",
    "originalName": "string",
    "filePath": "string",
    "mdContent": "# Document\n\nContent...",
    "fileSize": 1024,
    "fileType": "application/pdf",
    "converted": true,
    "userId": 1,
    "createdAt": "2026-04-13T17:00:00",
    "updatedAt": "2026-04-13T17:00:00"
  }
]
```
**失败响应**：
```json
{
  "error": "User not found"
}
```

### 2.4 获取文档详情

**请求方法**：GET
**请求路径**：`/api/documents/{id}`
**请求头**：
```
Authorization: Bearer {token}
```
**成功响应**：
```json
{
  "id": 1,
  "title": "string",
  "originalName": "string",
  "filePath": "string",
  "mdContent": "# Document\n\nContent...",
  "fileSize": 1024,
  "fileType": "application/pdf",
  "converted": true,
  "userId": 1,
  "createdAt": "2026-04-13T17:00:00",
  "updatedAt": "2026-04-13T17:00:00"
}
```
**失败响应**：
```json
{
  "error": "Document not found"
}
```

### 2.5 下载文档

**请求方法**：GET
**请求路径**：`/api/documents/{id}/download`
**请求头**：
```
Authorization: Bearer {token}
```
**成功响应**：
- 二进制文件下载

**失败响应**：
- HTTP 400 Bad Request

### 2.6 删除文档

**请求方法**：DELETE
**请求路径**：`/api/documents/{id}`
**请求头**：
```
Authorization: Bearer {token}
```
**成功响应**：
- HTTP 204 No Content

**失败响应**：
```json
{
  "error": "Document not found"
}
```

### 2.7 更新文档

**请求方法**：PUT
**请求路径**：`/api/documents/{id}`
**请求头**：
```
Authorization: Bearer {token}
```
**请求体**：
```json
{
  "title": "string"
}
```
**成功响应**：
```json
{
  "id": 1,
  "title": "string",
  "originalName": "string",
  "filePath": "string",
  "mdContent": "# Document\n\nContent...",
  "fileSize": 1024,
  "fileType": "application/pdf",
  "converted": true,
  "userId": 1,
  "createdAt": "2026-04-13T17:00:00",
  "updatedAt": "2026-04-13T17:00:00"
}
```
**失败响应**：
```json
{
  "error": "Document not found"
}
```

## 3. 错误响应格式

所有API接口的错误响应格式如下：

```json
{
  "error": "错误信息"
}
```

## 4. 认证机制

- 使用JWT（JSON Web Token）进行认证
- 登录成功后，服务器会返回一个JWT令牌
- 后续请求需要在请求头中携带该令牌：`Authorization: Bearer {token}`
- 令牌有效期为24小时（可在配置文件中修改）

## 5. 支持的文件格式

- PDF (.pdf)
- Word (.docx)
- Excel (.xlsx)

## 6. 文档转换

- 支持将上述格式的文档转换为Markdown格式
- 转换后的Markdown内容存储在文档实体的`mdContent`字段中
- 转换状态通过`converted`字段表示（true表示已转换，false表示未转换）