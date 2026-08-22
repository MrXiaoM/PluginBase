#!/usr/bin/env python3
"""将 JMH JSON 结果转换为 SparrowMiniMessage 与 DefaultMiniMessage 的 HTML 对比报告。"""

import argparse
import datetime as datetime_module
import html
import json
import math
from pathlib import Path


IMPLEMENTATIONS = ("sparrow", "default")
IMPLEMENTATION_NAMES = {"sparrow": "SparrowMiniMessage", "default": "DefaultMiniMessage"}
MODE_NAMES = {"thrpt": "吞吐量", "avgt": "平均延迟"}


def parse_arguments():
    parser = argparse.ArgumentParser(description="生成 MiniMessage JMH HTML 对比报告")
    parser.add_argument("input", type=Path, help="JMH JSON 结果文件")
    parser.add_argument("output", type=Path, help="输出 HTML 报告文件")
    return parser.parse_args()


def split_benchmark(name):
    method = name.rsplit(".", 1)[-1]
    for implementation in IMPLEMENTATIONS:
        if method.startswith(implementation):
            suffix = method[len(implementation):]
            return implementation, suffix
    return None, method


def display_name(suffix):
    if not suffix:
        return "未命名场景"
    result = []
    for index, character in enumerate(suffix):
        if index and character.isupper() and suffix[index - 1].islower():
            result.append(" ")
        result.append(character)
    names = {
        "Builder Configuration": "构建器完整配置",
        "Deserialize": "反序列化",
        "Dynamic Tag Cold": "动态无参标签：冷态",
        "Dynamic Tag Hot": "动态无参标签：缓存命中",
        "Dynamic Tag Miss Hot": "动态无参标签：缓存未命中",
        "Large Static Resolver": "16 个静态解析器",
        "Serialize Basic": "序列化：基础组件",
        "Serialize Styled": "序列化：样式组件",
        "Serialize Interactive": "序列化：交互组件",
        "Serialize Gradient": "序列化：渐变组件",
    }
    spaced = "".join(result)
    return names.get(spaced, spaced)


def scenario_label(params):
    scenario = params.get("scenario")
    if scenario is None:
        return "—"
    names = {
        "basic": "基础文本",
        "styled": "常用样式",
        "interactive": "嵌套交互事件",
        "custom": "自定义标签",
        "gradient": "渐变策略",
    }
    return names.get(str(scenario), str(scenario))


def to_number(value):
    if value is None:
        return None
    if isinstance(value, bool):
        return None
    if isinstance(value, (int, float)):
        number = float(value)
        return None if math.isnan(number) or math.isinf(number) else number
    text = str(value).strip()
    if not text or text.lower() in {"nan", "inf", "-inf", "infinity", "-infinity"}:
        return None
    try:
        number = float(text)
    except ValueError:
        return None
    return None if math.isnan(number) or math.isinf(number) else number


def metric_value(metric):
    if not metric:
        return None
    return {
        "score": to_number(metric.get("score")),
        "error": to_number(metric.get("scoreError")),
        "unit": metric.get("scoreUnit", ""),
    }


def format_number(value, digits=3):
    number = to_number(value)
    if number is None:
        return "—"
    return f"{number:,.{digits}f}"


def format_metric(metric, include_error=True):
    if metric is None or metric["score"] is None:
        return "—"
    rendered = f"{format_number(metric['score'])} {html.escape(str(metric['unit']))}"
    if include_error and metric["error"] is not None:
        rendered += f" ± {format_number(metric['error'])}"
    return rendered


def ratio(left, right):
    if left is None or right is None:
        return None
    left_score = to_number(left.get("score") if isinstance(left, dict) else None)
    right_score = to_number(right.get("score") if isinstance(right, dict) else None)
    if left_score in (None, 0) or right_score is None:
        return None
    return right_score / left_score


def compare_label(mode, default_metric, sparrow_metric):
    if default_metric is None or sparrow_metric is None:
        return "数据不完整", "neutral"
    relative = ratio(default_metric, sparrow_metric)
    if relative is None:
        return "数据不完整", "neutral"
    if mode == "avgt":
        speedup = 1 / relative
        if speedup > 1:
            return f"Sparrow 快 {speedup:.2f}×", "sparrow"
        if speedup < 1:
            return f"Default 快 {1 / speedup:.2f}×", "default"
    elif mode == "thrpt":
        if relative > 1:
            return f"Sparrow 高 {relative:.2f}×", "sparrow"
        if relative < 1:
            return f"Default 高 {1 / relative:.2f}×", "default"
    return "结果持平", "neutral"


def allocation_label(default_metric, sparrow_metric):
    relative = ratio(default_metric, sparrow_metric)
    if relative is None:
        return "数据不完整", "neutral"
    if relative < 1:
        return f"Sparrow 少分配 {1 / relative:.2f}×", "sparrow"
    if relative > 1:
        return f"Default 少分配 {relative:.2f}×", "default"
    return "分配量持平", "neutral"


def collect_records(raw_records):
    records = {}
    metadata = None
    for record in raw_records:
        implementation, suffix = split_benchmark(record["benchmark"])
        if implementation is None:
            continue
        if metadata is None:
            metadata = record
        params = record.get("params", {})
        key = (suffix, scenario_label(params), record.get("mode"))
        entry = records.setdefault(key, {"suffix": suffix, "scenario": scenario_label(params), "mode": record.get("mode"), "metrics": {}})
        entry["metrics"][implementation] = {
            "primary": metric_value(record.get("primaryMetric")),
            "allocation": metric_value(record.get("secondaryMetrics", {}).get("gc.alloc.rate.norm")),
            "allocation_rate": metric_value(record.get("secondaryMetrics", {}).get("gc.alloc.rate")),
            "gc_count": metric_value(record.get("secondaryMetrics", {}).get("gc.count")),
            "gc_time": metric_value(record.get("secondaryMetrics", {}).get("gc.time")),
        }
    return list(records.values()), metadata


def summary_rows(entries, mode):
    rows = []
    for entry in sorted((item for item in entries if item["mode"] == mode), key=lambda item: (item["suffix"], item["scenario"])):
        default = entry["metrics"].get("default", {})
        sparrow = entry["metrics"].get("sparrow", {})
        default_primary = default.get("primary")
        sparrow_primary = sparrow.get("primary")
        default_allocation = default.get("allocation")
        sparrow_allocation = sparrow.get("allocation")
        comparison, comparison_class = compare_label(mode, default_primary, sparrow_primary)
        allocation, allocation_class = allocation_label(default_allocation, sparrow_allocation)
        rows.append(
            "<tr>"
            f"<td>{html.escape(display_name(entry['suffix']))}</td>"
            f"<td>{html.escape(entry['scenario'])}</td>"
            f"<td>{format_metric(default_primary)}</td>"
            f"<td>{format_metric(sparrow_primary)}</td>"
            f"<td><span class=\"badge {comparison_class}\">{html.escape(comparison)}</span></td>"
            f"<td>{format_metric(default_allocation)}</td>"
            f"<td>{format_metric(sparrow_allocation)}</td>"
            f"<td><span class=\"badge {allocation_class}\">{html.escape(allocation)}</span></td>"
            "</tr>"
        )
    return "\n".join(rows) or "<tr><td colspan=\"8\">未找到该指标的配对记录。</td></tr>"


def detail_rows(entries):
    rows = []
    for entry in sorted(entries, key=lambda item: (item["suffix"], item["scenario"], item["mode"])):
        for implementation in IMPLEMENTATIONS:
            metrics = entry["metrics"].get(implementation)
            if metrics is None:
                continue
            rows.append(
                "<tr>"
                f"<td>{html.escape(display_name(entry['suffix']))}</td>"
                f"<td>{html.escape(entry['scenario'])}</td>"
                f"<td>{html.escape(MODE_NAMES.get(entry['mode'], entry['mode']))}</td>"
                f"<td>{html.escape(IMPLEMENTATION_NAMES[implementation])}</td>"
                f"<td>{format_metric(metrics['primary'])}</td>"
                f"<td>{format_metric(metrics['allocation'])}</td>"
                f"<td>{format_metric(metrics['allocation_rate'])}</td>"
                f"<td>{format_metric(metrics['gc_count'])}</td>"
                f"<td>{format_metric(metrics['gc_time'])}</td>"
                "</tr>"
            )
    return "\n".join(rows)


def metadata_items(metadata, count):
    if metadata is None:
        return "<li>未找到可识别的 JMH 记录。</li>"
    jvm = metadata.get("jvm", "未知")
    return "\n".join([
        f"<li><strong>JMH：</strong>{html.escape(str(metadata.get('jmhVersion', '未知')))}</li>",
        f"<li><strong>JDK：</strong>{html.escape(str(metadata.get('jdkVersion', '未知')))}，{html.escape(str(metadata.get('vmName', '未知')))}</li>",
        f"<li><strong>JVM：</strong><code>{html.escape(str(jvm))}</code></li>",
        f"<li><strong>线程：</strong>{html.escape(str(metadata.get('threads', '未知')))}</li>",
        f"<li><strong>Fork：</strong>{html.escape(str(metadata.get('forks', '未知')))}</li>",
        f"<li><strong>预热：</strong>{html.escape(str(metadata.get('warmupIterations', '未知')))} × {html.escape(str(metadata.get('warmupTime', '未知')))}</li>",
        f"<li><strong>测量：</strong>{html.escape(str(metadata.get('measurementIterations', '未知')))} × {html.escape(str(metadata.get('measurementTime', '未知')))}</li>",
        f"<li><strong>配对记录：</strong>{count} 组</li>",
    ])


def build_html(entries, metadata):
    generated_at = datetime_module.datetime.now().astimezone().strftime("%Y-%m-%d %H:%M:%S %z")
    average_rows = summary_rows(entries, "avgt")
    throughput_rows = summary_rows(entries, "thrpt")
    details = detail_rows(entries)
    metadata_html = metadata_items(metadata, len(entries))
    return f"""<!doctype html>
<html lang=\"zh-CN\">
<head>
<meta charset=\"utf-8\">
<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">
<title>SparrowMiniMessage 性能对比</title>
<style>
:root {{ color-scheme: light; --bg: #f5f7fb; --panel: #ffffff; --text: #172033; --muted: #68738a; --line: #dce2ed; --sparrow: #087d71; --default: #b45309; --accent: #4f46e5; }}
* {{ box-sizing: border-box; }}
body {{ margin: 0; background: var(--bg); color: var(--text); font: 14px/1.55 Inter, "Microsoft YaHei", sans-serif; }}
header {{ background: linear-gradient(130deg, #172554, #4f46e5); color: #fff; padding: 46px max(24px, calc((100vw - 1440px) / 2)); }}
h1 {{ margin: 0; font-size: clamp(26px, 4vw, 38px); }}
header p {{ margin: 10px 0 0; color: #dbeafe; font-size: 16px; }}
main {{ max-width: 1440px; margin: 28px auto 60px; padding: 0 20px; }}
section {{ background: var(--panel); border: 1px solid var(--line); border-radius: 14px; margin: 20px 0; overflow: hidden; box-shadow: 0 4px 16px #1e293b0b; }}
section > h2 {{ margin: 0; padding: 18px 22px; border-bottom: 1px solid var(--line); font-size: 20px; }}
.content {{ padding: 20px 22px; }}
.metadata {{ display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 8px 26px; padding: 0; list-style: none; }}
.table-wrap {{ overflow-x: auto; }}
table {{ width: 100%; border-collapse: collapse; min-width: 1040px; }}
th, td {{ padding: 11px 12px; border-bottom: 1px solid var(--line); text-align: left; vertical-align: top; white-space: nowrap; }}
th {{ background: #f8fafc; color: #475569; font-size: 12px; letter-spacing: .03em; }}
tr:hover td {{ background: #f8fbff; }}
.badge {{ display: inline-block; border-radius: 999px; padding: 3px 9px; font-weight: 700; font-size: 12px; }}
.badge.sparrow {{ color: #066c5f; background: #d9f8ee; }}
.badge.default {{ color: #9a4b0d; background: #fff0d7; }}
.badge.neutral {{ color: #536176; background: #e8edf5; }}
.note {{ color: var(--muted); margin: 0 0 14px; }}
.warning {{ border-left: 4px solid #f59e0b; background: #fffbeb; padding: 12px 14px; color: #78350f; border-radius: 4px; }}
code {{ background: #eef2ff; padding: 1px 4px; border-radius: 4px; overflow-wrap: anywhere; }}
footer {{ color: var(--muted); text-align: center; margin-top: 28px; }}
</style>
</head>
<body>
<header>
  <h1>SparrowMiniMessage 与 DefaultMiniMessage 性能对比</h1>
  <p>基于 JMH 原始 JSON 自动生成 · 生成时间：{html.escape(generated_at)}</p>
</header>
<main>
<section>
  <h2>实验记录</h2>
  <div class=\"content\"><ul class=\"metadata\">{metadata_html}</ul></div>
</section>
<section>
  <h2>如何阅读</h2>
  <div class=\"content\">
    <p class=\"note\">平均延迟越低越好；吞吐量越高越好；每操作分配量越低越好。相对比值仅在同一场景、同一 JMH 模式、同一运行环境下比较。</p>
    <div class=\"warning\">渐变项目的两端可见输出一致，但 Adventure 5.2.0 下内部 <code>Component</code> 图布局不同。因此，渐变结果仅反映各实现处理自身组件表示的成本，不能视为严格同构对象的直接速度排名。</div>
  </div>
</section>
<section>
  <h2>平均延迟与分配量</h2>
  <div class=\"content table-wrap\">
    <table><thead><tr><th>项目</th><th>输入场景</th><th>Default 平均延迟</th><th>Sparrow 平均延迟</th><th>延迟结论</th><th>Default 分配/操作</th><th>Sparrow 分配/操作</th><th>分配结论</th></tr></thead>
    <tbody>{average_rows}</tbody></table>
  </div>
</section>
<section>
  <h2>吞吐量与分配量</h2>
  <div class=\"content table-wrap\">
    <table><thead><tr><th>项目</th><th>输入场景</th><th>Default 吞吐量</th><th>Sparrow 吞吐量</th><th>吞吐量结论</th><th>Default 分配/操作</th><th>Sparrow 分配/操作</th><th>分配结论</th></tr></thead>
    <tbody>{throughput_rows}</tbody></table>
  </div>
</section>
<section>
  <h2>原始指标明细</h2>
  <div class=\"content table-wrap\">
    <table><thead><tr><th>项目</th><th>输入场景</th><th>模式</th><th>实现</th><th>主指标</th><th>分配/操作</th><th>分配速率</th><th>GC 次数</th><th>GC 时间</th></tr></thead>
    <tbody>{details}</tbody></table>
  </div>
</section>
<footer>报告由 <code>modules/message/tests/adventure050200/tools/generate_jmh_report.py</code> 生成。</footer>
</main>
</body>
</html>
"""


def main():
    arguments = parse_arguments()
    with arguments.input.open("r", encoding="utf-8") as input_file:
        raw_records = json.load(input_file)
    entries, metadata = collect_records(raw_records)
    document = build_html(entries, metadata)
    arguments.output.parent.mkdir(parents=True, exist_ok=True)
    arguments.output.write_text(document, encoding="utf-8")
    print(f"已生成 {arguments.output}，包含 {len(entries)} 组配对记录。")


if __name__ == "__main__":
    main()
