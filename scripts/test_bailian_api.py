#!/usr/bin/env python3
"""
百炼 API 连接测试脚本
用于验证 API Key 和模型配置是否正确
"""

import json
import urllib.request
import urllib.error

# 配置
API_KEY = "sk-sp-8dd0de13d53b4e5fa0a092dd8e9b751a"
MODEL = "qwen3.5-plus"

# 备选模型 (如果当前模型不可用)
# MODEL = "qwen3-coder-plus"
# MODEL = "qwen3-max-2026-01-23"
API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation"

def test_connection():
    """测试 API 连接"""
    print("=" * 50)
    print("百炼 API 连接测试")
    print("=" * 50)
    print(f"API Key: {API_KEY[:15]}...{API_KEY[-10:]}")
    print(f"Model: {MODEL}")
    print(f"API URL: {API_URL}")
    print("=" * 50)
    
    # 构建请求
    request_body = {
        "model": MODEL,
        "input": {
            "messages": [
                {"role": "system", "content": "你是一个专业的助手。"},
                {"role": "user", "content": "你好，请回复'测试成功'"}
            ]
        },
        "parameters": {
            "temperature": 0.1,
            "max_tokens": 100
        }
    }
    
    try:
        # 发送请求
        print("\n📡 发送请求...")
        req = urllib.request.Request(
            API_URL,
            data=json.dumps(request_body).encode('utf-8'),
            headers={
                "Content-Type": "application/json",
                "Authorization": f"Bearer {API_KEY}"
            }
        )
        
        with urllib.request.urlopen(req, timeout=10) as response:
            result = json.loads(response.read().decode('utf-8'))
        
        # 解析响应
        print("✅ 请求成功！")
        print("\n📋 响应内容:")
        print(json.dumps(result, indent=2, ensure_ascii=False))
        
        # 提取回复
        if "output" in result and "text" in result["output"]:
            reply = result["output"]["text"]
            print(f"\n💬 AI 回复：{reply}")
            print("\n✅ 测试通过！API 配置正确。")
            return True
        else:
            print("\n⚠️ 响应格式异常")
            return False
            
    except urllib.error.HTTPError as e:
        print(f"\n❌ HTTP 错误：{e.code}")
        print(f"错误内容：{e.read().decode('utf-8')}")
        return False
    except urllib.error.URLError as e:
        print(f"\n❌ 网络错误：{e.reason}")
        return False
    except Exception as e:
        print(f"\n❌ 未知错误：{e}")
        return False

def test_classification():
    """测试分类功能"""
    print("\n" + "=" * 50)
    print("交易分类测试")
    print("=" * 50)
    
    test_cases = [
        ("星巴克", 35.0, "餐饮"),
        ("滴滴出行", 25.0, "交通"),
        ("淘宝", 199.0, "购物"),
        ("万达影城", 50.0, "娱乐"),
    ]
    
    prompt_template = """
    你是一个智能记账助手，请根据商户名称判断消费分类。
    
    商户名称：{merchant}
    交易金额：¥{amount}
    可选分类：餐饮，交通，购物，娱乐，住房，医疗，教育，其他
    
    请只返回 JSON 格式：
    {{
        "category": "分类名称",
        "confidence": 0.95,
        "reason": "判断理由"
    }}
    """
    
    for merchant, amount, expected in test_cases:
        print(f"\n测试：{merchant} (¥{amount})")
        print(f"预期分类：{expected}")
        
        # 简化测试，不实际调用 API
        print(f"⏭️  跳过实际调用 (需要在 Android 环境中运行)")
        print(f"✅ 测试用例准备完成")

if __name__ == "__main__":
    success = test_connection()
    test_classification()
    
    print("\n" + "=" * 50)
    print("测试总结")
    print("=" * 50)
    if success:
        print("✅ API 连接测试：通过")
        print("✅ 配置状态：正确")
        print("\n🎉 可以开始使用 LLM 分类功能！")
    else:
        print("❌ API 连接测试：失败")
        print("\n⚠️ 请检查:")
        print("1. API Key 是否正确")
        print("2. 网络连接是否正常")
        print("3. 模型名称是否正确")
    print("=" * 50)
