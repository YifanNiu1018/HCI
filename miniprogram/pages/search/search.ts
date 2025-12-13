Page({
    data: {
      mode: 'recipe' // recipe | ingredient
    },
  
    switchMode(e) {
      const { mode } = e.currentTarget.dataset
      if (mode === this.data.mode) return
  
      this.setData({ mode })
    }
  })
  