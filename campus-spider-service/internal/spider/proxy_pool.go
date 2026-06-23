package spider

import "sync/atomic"

type ProxyPool struct {
	proxies []string
	idx     uint64
}

// NewProxyPool 创建代理池
func NewProxyPool(proxies []string) *ProxyPool {
	return &ProxyPool{proxies: proxies}
}

// Pick 从代理池中获取代理
func (p *ProxyPool) Pick() string {
	if p == nil || len(p.proxies) == 0 {
		return ""
	}
	// 从代理池中获取代理
	i := atomic.AddUint64(&p.idx, 1) - 1
	// 返回代理
	return p.proxies[int(i%uint64(len(p.proxies)))]
}
