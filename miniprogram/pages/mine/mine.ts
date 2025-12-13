Page({
    data: {
      items: Array.from({ length: 12 }, (_, i) => i)
    },
  
    onReachBottom() {
      const more = Array.from(
        { length: 6 },
        (_, i) => this.data.items.length + i
      );
      this.setData({
        items: [...this.data.items, ...more]
      });
    },
    goEdit(e: WechatMiniprogram.BaseEvent) {
        const id = (e.currentTarget as any).dataset.id
      
        wx.navigateTo({
          url: `/pages/edit/edit?id=${id}`
        })
      }
  });
  