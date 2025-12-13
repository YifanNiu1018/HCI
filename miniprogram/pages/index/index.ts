Page({
    data: {
      items: Array.from({ length: 10 }, (_, i) => i)
    },
  
    onReachBottom() {
      const more = Array.from({ length: 6 }, (_, i) => this.data.items.length + i);
      this.setData({
        items: [...this.data.items, ...more]
      });
    }
  });
  