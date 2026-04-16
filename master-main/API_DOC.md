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
  "accessToken": "string",
  "username": "string"
}
```
**失败响应**：
```json
{
  "error": "Username already exists",
  "status": 400,
  "timestamp": 1713039600000,
  "traceId": "uuid"
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
  "accessToken": "string",
  "username": "string"
}
```
**失败响应**：
```json
{
  "error": "Invalid username or password",
  "status": 401,
  "timestamp": 1713039600000,
  "traceId": "uuid"
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
  "data": {
    "id": 1,
    "title": "string",
    "originalName": "string",
    "filePath": "string",
    "mdContent": "# Document\n\nContent...",
    "fileSize": 1024,
    "fileType": "application/pdf",
    "converted": true,
    "user": {
      "id": 1,
      "username": "string"
    },
    "createdAt": "2026-04-13T17:00:00",
    "updatedAt": "2026-04-13T17:00:00"
  },
  "status": 201,
  "timestamp": 1713039600000,
  "traceId": "uuid"
}
```
**失败响应**：
```json
{
  "error": "User not found",
  "status": 401,
  "timestamp": 1713039600000,
  "traceId": "uuid"
}
```
或
```json
{
  "error": "Failed to upload document",
  "status": 500,
  "timestamp": 1713039600000,
  "traceId": "uuid"
}
```
或
```json
{
  "error": "不支持的文件类型: application/unknown",
  "status": 400,
  "timestamp": 1713039600000,
  "traceId": "uuid"
}
```

### 2.2 获取文档列表

**请求方法**：GET
**请求路径**：`/api/documents?page=0&size=10`
**请求头**：
```
Authorization: Bearer {token}
```
**成功响应**：
```json
{
  "data": {
    "documents": [
      {
        "id": 1,
        "title": "string",
        "originalName": "string",
        "filePath": "string",
        "mdContent": "# Document\n\nContent...",
        "fileSize": 1024,
        "fileType": "application/pdf",
        "converted": true,
        "user": {
          "id": 1,
          "username": "string"
        },
        "createdAt": "2026-04-13T17:00:00",
        "updatedAt": "2026-04-13T17:00:00"
      }
    ],
    "total": 1,
    "totalPages": 1,
    "page": 0,
    "size": 10
  },
  "status": 200,
  "timestamp": 1713039600000,
  "traceId": "uuid"
}
```
**失败响应**：
```json
{
  "error": "User not found",
  "status": 401,
  "timestamp": 1713039600000,
  "traceId": "uuid"
}
```
或
```json
{
  "error": "分页参数不合法",
  "status": 400,
  "timestamp": 1713039600000,
  "traceId": "uuid"
}
```
或
```json
{
  "error": "当前页码超出范围",
  "status": 400,
  "timestamp": 1713039600000,
  "traceId": "uuid"
}
```

### 2.3 获取文档详情

**请求方法**：GET
**请求路径**：`/api/documents/{id}`
**请求头**：
```
Authorization: Bearer {token}
```
**成功响应**：
```json
{
  "data": {
    "id": 1,
    "title": "string",
    "originalName": "string",
    "filePath": "string",
    "mdContent": "# Document\n\nContent...",
    "fileSize": 1024,
    "fileType": "application/pdf",
    "converted": true,
    "user": {
      "id": 1,
      "username": "string"
    },
    "createdAt": "2026-04-13T17:00:00",
    "updatedAt": "2026-04-13T17:00:00"
  },
  "status": 200,
  "timestamp": 1713039600000,
  "traceId": "uuid"
}
```
**失败响应**：
```json
{
  "error": "User not found",
  "status": 401,
  "timestamp": 1713039600000,
  "traceId": "uuid"
}
```
或
```json
{
  "error": "Failed to convert document",
  "status": 500,
  "timestamp": 1713039600000,
  "traceId": "uuid"
}
```
或
```json
{
  "error": "Document not found",
  "status": 400,
  "timestamp": 1713039600000,
  "traceId": "uuid"
}
```

### 2.4 下载文档

**请求方法**：GET
**请求路径**：`/api/documents/{id}/download`
**请求头**：
```
Authorization: Bearer {token}
```
**成功响应**：
- 二进制文件下载

**失败响应**：
- HTTP 401 Unauthorized（用户不存在）
- HTTP 400 Bad Request（其他错误）

### 2.5 删除文档

**请求方法**：DELETE
**请求路径**：`/api/documents/{id}`
**请求头**：
```
Authorization: Bearer {token}
```
**成功响应**：
```json
{
  "data": {
    "message": "Document deleted successfully"
  },
  "status": 200,
  "timestamp": 1713039600000,
  "traceId": "uuid"
}
```
**失败响应**：
```json
{
  "error": "User not found",
  "status": 401,
  "timestamp": 1713039600000,
  "traceId": "uuid"
}
```
或
```json
{
  "error": "Document not found",
  "status": 400,
  "timestamp": 1713039600000,
  "traceId": "uuid"
}
```

### 2.6 更新文档

**请求方法**：PUT
**请求路径**：`/api/documents/{id}`
**请求头**：
```
Authorization: Bearer {token}
```
**请求体**：
```json
{
  "title": "string",
  "mdContent": "# Document\n\nNew content..."
}
```
**成功响应**：
```json
{
  "data": {
    "id": 1,
    "title": "string",
    "originalName": "string",
    "filePath": "string",
    "mdContent": "# Document\n\nNew content...",
    "fileSize": 1024,
    "fileType": "application/pdf",
    "converted": true,
    "user": {
      "id": 1,
      "username": "string"
    },
    "createdAt": "2026-04-13T17:00:00",
    "updatedAt": "2026-04-13T17:00:00"
  },
  "status": 200,
  "timestamp": 1713039600000,
  "traceId": "uuid"
}
```
**失败响应**：
```json
{
  "error": "User not found",
  "status": 401,
  "timestamp": 1713039600000,
  "traceId": "uuid"
}
```
或
```json
{
  "error": "Document not found",
  "status": 400,
  "timestamp": 1713039600000,
  "traceId": "uuid"
}
```

## 3. 错误响应格式

所有API接口的错误响应格式如下：

```json
{
  "error": "错误信息",
  "status": 错误状态码,
  "timestamp": 时间戳,
  "traceId": "跟踪ID"
}
```

## 4. 认证机制

- 使用JWT（JSON Web Token）进行认证
- 登录成功后，服务器会返回一个JWT令牌
- 后续请求需要在请求头中携带该令牌：`Authorization: Bearer {token}`
- 令牌有效期为24小时（可在配置文件中修改）

## 5. 支持的文件格式

- PDF (.pdf)
- Word (.doc, .docx)
- Excel (.xls, .xlsx)
- PowerPoint (.ppt, .pptx)
- Markdown (.md)
- 代码文件（.java, .py, .js, .html, .css, .json, .xml, .txt 等）
- 图片（.jpg, .jpeg, .png, .gif, .bmp, .webp）

## 6. 文档转换

- 支持将上述格式的文档转换为Markdown格式
- **上传即处理**：文件上传后会立即自动转换为Markdown
- 转换后的Markdown内容存储在文档实体的`mdContent`字段中
- 转换状态通过`converted`字段表示（true表示已转换，false表示未转换）
