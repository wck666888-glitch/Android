/**
 * IR Remote Configuration Server
 * 
 * 提供IR配置的RESTful API
 * 
 * 启动方式:
 *   npm install
 *   npm start
 * 
 * 默认端口: 3000
 */

const express = require('express');
const cors = require('cors');
const fs = require('fs');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000;

// 中间件
app.use(cors());
app.use(express.json());

// 配置文件目录
const CONFIGS_DIR = path.join(__dirname, 'configs');

// 确保配置目录存在
if (!fs.existsSync(CONFIGS_DIR)) {
    fs.mkdirSync(CONFIGS_DIR, { recursive: true });
}

/**
 * 获取所有配置的元数据
 * GET /api/configs
 */
app.get('/api/configs', (req, res) => {
    try {
        const files = fs.readdirSync(CONFIGS_DIR).filter(f => f.endsWith('.json'));
        const configs = files.map(file => {
            const content = fs.readFileSync(path.join(CONFIGS_DIR, file), 'utf8');
            const config = JSON.parse(content);
            return {
                id: config.id,
                name: config.name,
                description: config.description || '',
                version: config.version || '1.0',
                updated_at: config.updated_at || Date.now()
            };
        });
        res.json(configs);
    } catch (error) {
        console.error('Error reading configs:', error);
        res.status(500).json({ error: 'Failed to read configs' });
    }
});

/**
 * 获取指定配置
 * GET /api/configs/:id
 */
app.get('/api/configs/:id', (req, res) => {
    try {
        const configId = req.params.id;
        const filePath = path.join(CONFIGS_DIR, `${configId}.json`);

        if (!fs.existsSync(filePath)) {
            return res.status(404).json({ error: 'Config not found' });
        }

        const content = fs.readFileSync(filePath, 'utf8');
        const config = JSON.parse(content);
        res.json(config);
    } catch (error) {
        console.error('Error reading config:', error);
        res.status(500).json({ error: 'Failed to read config' });
    }
});

/**
 * 获取默认配置
 * GET /api/configs/default
 */
app.get('/api/configs/default', (req, res) => {
    try {
        const files = fs.readdirSync(CONFIGS_DIR).filter(f => f.endsWith('.json'));

        for (const file of files) {
            const content = fs.readFileSync(path.join(CONFIGS_DIR, file), 'utf8');
            const config = JSON.parse(content);
            if (config.is_default) {
                return res.json(config);
            }
        }

        // 如果没有默认配置，返回第一个
        if (files.length > 0) {
            const content = fs.readFileSync(path.join(CONFIGS_DIR, files[0]), 'utf8');
            return res.json(JSON.parse(content));
        }

        res.status(404).json({ error: 'No default config found' });
    } catch (error) {
        console.error('Error reading default config:', error);
        res.status(500).json({ error: 'Failed to read default config' });
    }
});

/**
 * 上传/更新配置
 * POST /api/configs
 */
app.post('/api/configs', (req, res) => {
    try {
        const config = req.body;

        if (!config.id || !config.name) {
            return res.status(400).json({ error: 'Missing required fields' });
        }

        config.updated_at = Date.now();

        const filePath = path.join(CONFIGS_DIR, `${config.id}.json`);
        fs.writeFileSync(filePath, JSON.stringify(config, null, 2));

        res.json({ success: true, id: config.id });
    } catch (error) {
        console.error('Error saving config:', error);
        res.status(500).json({ error: 'Failed to save config' });
    }
});

/**
 * 删除配置
 * DELETE /api/configs/:id
 */
app.delete('/api/configs/:id', (req, res) => {
    try {
        const configId = req.params.id;
        const filePath = path.join(CONFIGS_DIR, `${configId}.json`);

        if (!fs.existsSync(filePath)) {
            return res.status(404).json({ error: 'Config not found' });
        }

        fs.unlinkSync(filePath);
        res.json({ success: true });
    } catch (error) {
        console.error('Error deleting config:', error);
        res.status(500).json({ error: 'Failed to delete config' });
    }
});

/**
 * 健康检查
 * GET /health
 */
app.get('/health', (req, res) => {
    res.json({ status: 'ok', timestamp: Date.now() });
});

// 启动服务器
app.listen(PORT, '0.0.0.0', () => {
    console.log(`====================================`);
    console.log(`IR Config Server is running!`);
    console.log(`Port: ${PORT}`);
    console.log(`Configs directory: ${CONFIGS_DIR}`);
    console.log(`====================================`);
    console.log(`\nAvailable endpoints:`);
    console.log(`  GET  /api/configs        - List all configs`);
    console.log(`  GET  /api/configs/:id    - Get config by ID`);
    console.log(`  GET  /api/configs/default - Get default config`);
    console.log(`  POST /api/configs        - Upload config`);
    console.log(`  DELETE /api/configs/:id  - Delete config`);
    console.log(`  GET  /health             - Health check`);
});
