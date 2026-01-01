package top.mrxiaom.pluginbase.sql.sentence;

import top.mrxiaom.pluginbase.sql.api.MethodContext;
import top.mrxiaom.pluginbase.sql.api.Variable;
import top.mrxiaom.pluginbase.sql.nanoxml.XMLElement;
import top.mrxiaom.pluginbase.sql.nanoxml.XMLParseException;
import top.mrxiaom.pluginbase.sql.utils.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

public class StatementHelper {
    private static final Pattern regexVariable = Pattern.compile("#\\{([A-Za-z0-9_]+)}");

    /**
     * 将输入的语句解析为 XML
     */
    public static List<XMLElement> parseScript(String sentence) throws XMLParseException {
        XMLElement xml = new XMLElement();
        xml.parseString("<PluginBaseSQL>\n" + sentence + "\n</PluginBaseSQL>");
        Vector<XMLElement> children = xml.getChildren();
        List<XMLElement> elements = new ArrayList<>();
        if (children.isEmpty()) {
            XMLElement child = xml.createAnotherElement();
            child.setName("content");
            child.setContent(xml.getContent());
        } else {
            elements.addAll(children);
        }
        return elements;
    }

    /**
     * 运行构建语句脚本，获取当前情况下最终可读语句
     * @param sentenceParts 语句分割部分
     * @param variables 变量声明
     * @param ctx 方法调用上下文
     */
    public static String doRunScript(List<XMLElement> sentenceParts, Map<String, Variable> variables, MethodContext ctx) throws IllegalStateException {
        StringBuilder sb = new StringBuilder();
        for (XMLElement element : sentenceParts) {
            String type = element.getName();
            if (type.equals("content")) {
                sb.append(element.getContent());
            }
            if (type.equals("script")) {
                String conditionStatement = (String) element.getAttribute("if");
                if (conditionStatement == null) {
                    throw new IllegalStateException("未能从 <script> 中解析 if 声明");
                }
                // TODO: 替换变量并执行判定，是否应该将语句添加进去

            }
        }
        return sb.toString();
    }

    /**
     * 新建预构建语句
     * @param conn 数据库连接
     * @param sentence 原始输入语句
     * @param variables 变量声明
     * @param ctx 方法调用上下文
     */
    public static PreparedStatement prepare(
            Connection conn,
            String sentence,
            Map<String, Variable> variables,
            MethodContext ctx
    ) throws Throwable {
        List<XMLElement> elements = parseScript(sentence);
        String finalReadableSentence = doRunScript(elements, variables, ctx);
        StringBuilder sb = new StringBuilder(); // 解析变量为问号 (?) 并构建 PreparedStatement 的参数列表
        List<Variable> params = StringUtils.split(regexVariable, finalReadableSentence, (match) -> {
            if (!match.isMatched) {
                sb.append("?");
                String variableName = match.result.group(1);
                Variable variable = variables.get(variableName);
                if (variable == null) {
                    throw new IllegalStateException("找不到变量 '" + variableName + "'");
                }
                return variable;
            } else {
                sb.append(match.text);
                return null;
            }
        });
        PreparedStatement ps = conn.prepareStatement(sb.toString());
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i).get(ctx));
        }
        return ps;
    }

}
