#!/usr/bin/env node
/**
 * 提交前钩子脚本：
 * 当 back/src/main/resources/application.yml 被加入暂存区时，
 * 自动把其中的真实 API Key 替换为占位符，再写入 index。
 * 工作区文件保持真实 Key 不变。
 */
const { execSync } = require('child_process');

const APP_YML = 'back/src/main/resources/application.yml';
const PLACEHOLDER = 'sk-your-api-key-here';

function main() {
  // 只有在 application.yml 进入暂存区时才处理
  const stagedFiles = execSync('git diff --cached --name-only', { encoding: 'utf8' });
  if (!stagedFiles.includes(APP_YML)) {
    return;
  }

  // 读取暂存区中的文件内容
  let stagedContent;
  try {
    stagedContent = execSync(`git show :${APP_YML}`, { encoding: 'utf8' });
  } catch (err) {
    // 文件可能是新增或已被删除，无需处理
    return;
  }

  // 替换 api-key 行为占位符
  const replaced = stagedContent.replace(
    /^(\s*api-key:\s*)sk-[a-zA-Z0-9]+/m,
    `$1${PLACEHOLDER}`
  );

  if (replaced === stagedContent) {
    return;
  }

  // 将替换后的内容写入新 blob，并更新 index
  const blobId = execSync('git hash-object -w --stdin', {
    input: replaced,
    encoding: 'utf8'
  }).trim();

  execSync(`git update-index --cacheinfo 100644 ${blobId} ${APP_YML}`);
}

main();
