package tkngch.bookmarkManager.js.binder

import tkngch.bookmarkManager.common.model.Tag
import tkngch.bookmarkManager.common.model.TagName
import tkngch.bookmarkManager.common.model.Visibility

data class TagBinder(
    val onAdd: Binder2<TagName, Visibility> = Binder2(),
    val onClick: Binder1<Tag> = Binder1(),
    val onUpdate: Binder2<Tag, Visibility> = Binder2(),
    val onDelete: Binder1<Tag> = Binder1(),
    val onClickShowMore: Binder0 = Binder0(),
    val onClickShowLess: Binder0 = Binder0()
)
