#include <cstdio>
#include <algorithm>

using namespace std;

typedef pair<double,double> pdd;
typedef vector<pdd> vpd;

int main(void) {
	int n;
	double w, u, v, t1, t2;
	scanf("%d%lf%lf%lf%lf%lf", &n, &w, &u, &v, &t1, &t2);
	vpd ivals;
	for (int i = 0; i < n; ++i) {
		char s[10];
		scanf("%s", s);
		int d = *s == 'E' ? 1 : -1, m;
		scanf("%d", &m);
		for (int j = 0; j < m; ++j) {
			int l, p;
			scanf("%d%d", &l, &p);
			double t1 = -p*d/u, t2 = -(p-d*l)*d/u;
			if (t1 > t2) swap(t1, t2);
			ivals.push_back(pdd(t1-(i+1)*w/v, t2-i*w/v));
		}
	}
	ivals.push_back(pdd(t2, 1e90));
	sort(ivals.begin(), ivals.end());
	double at = 0, res = 0;
	for (pdd p: ivals) {
		res = max(res, p.first-at);
		at = max(at, p.second);
	}
	printf("%.8lf\n", res);
	return 0;
}
