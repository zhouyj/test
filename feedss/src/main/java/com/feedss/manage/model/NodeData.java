package com.feedss.manage.model;

import lombok.Data;

import java.util.List;

/**
 * Tree Node
 * @author : by AegisLee on 16/11/21.
 */
@Data
public class NodeData {

    private String title;   // 标题
    private String tooltip;   // Will be added as `title` attribute, thus enabling a tooltip.
    private String parentId;  // 父节点uuid.

    private boolean active; // 是否激活, 初始化用, 不持久化
    private Object data;    // 附加data,
    private boolean expanded;    // 是否展开

    private String extraClasses;    // Class names added to the node markup (separate with space).
    private boolean folder;
    private boolean hideCheckbox;

    private String icon;    // true: 默认icon; false: 隐藏icon; string: <span class="fancytree-custom-icon ui-icon ui-icon-heart" />
    private String key;     // Unique key for this node (auto-generated if omitted).
    private boolean lazy;   // Lazy folders call the `lazyLoad` on first expand to load their children

    private String statusNodeType;  // If set, make this node a status node. Values: 'error', 'loading', 'nodata', 'paging'.
    private int sort;  // If set, make this node a status node. Values: 'error', 'loading', 'nodata', 'paging'.

    private boolean showInHomePageModel;//是否在首页列表中展现
    private boolean showInRightModel;//是否在右侧栏展现
    private boolean visible;//是否可见

    private List<NodeData> children;

}
